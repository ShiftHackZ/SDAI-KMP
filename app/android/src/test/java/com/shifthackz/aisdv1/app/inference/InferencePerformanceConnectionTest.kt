package com.shifthackz.aisdv1.app.inference

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Message
import android.os.Messenger
import android.os.Process
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.ReflectionHelpers.ClassParameter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import kotlin.time.Duration.Companion.seconds

/**
 * Exercises the production host connection with controlled protocol envelopes. These unit tests
 * check telemetry isolation; Binder transport and native inference are covered by application QA.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
@LooperMode(LooperMode.Mode.PAUSED)
class InferencePerformanceConnectionTest {
    private val dispatcher = StandardTestDispatcher()
    private val sessions = mutableListOf<Session>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() = runTest(timeout = 10.seconds) {
        sessions.forEach {
            it.disconnect()
            it.connection.close()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun `given inference telemetry, no generation has submitted, expected absent reading without binding worker`() =
        runTest {
            // Given
            val session = Session().also(sessions::add)

            // When
            val reading = session.connection.readPerformance()

            // Then
            assertNull(reading)
            assertTrue(session.commands.isEmpty())
            assertFalse(session.binding.isCaptured)
        }

    @Test
    fun `given inference telemetry, matching response arrives, expected CPU RSS and validated incarnation returned`() =
        runTest {
            // Given
            val session = connect()
            val read = backgroundScope.async { session.connection.readPerformance() }
            pump()

            // When
            session.respond(session.requestId())
            pump()

            // Then
            assertEquals(
                ProcessPerformanceReading("99:${session.incarnation}", 200L, 400L),
                read.await()
            )
            assertTrue(session.generation.isActive)
        }

    @Test
    fun `given inference telemetry, counters are missing or negative, expected read failure without failing generation`() =
        runTest {
            // Given
            val session = connect()
            val failures = mutableListOf<Throwable?>()

            // When
            for (hasCounters in listOf(false, true)) {
                val read =
                    backgroundScope.async { runCatching { session.connection.readPerformance() } }
                pump()
                session.respond(session.requestId()) {
                    if (hasCounters) data.putLong(InferenceProtocol.CPU_NANOS, -1)
                    else data.remove(InferenceProtocol.RESIDENT_BYTES)
                }
                pump()
                failures.add(read.await().exceptionOrNull())
            }

            // Then
            assertTrue(failures.all { it is IllegalStateException })
            assertTrue(session.generation.isActive)
            assertFalse(session.commands.any { it.what == InferenceProtocol.CANCEL })
        }

    @Test
    fun `given inference telemetry, peer does not reply before deadline, expected timeout only for resource reading`() =
        runTest {
            // Given
            val session = connect()
            val read =
                backgroundScope.async { runCatching { session.connection.readPerformance() } }
            pump()

            // When
            advanceTimeBy(2_000)
            pump()

            // Then
            assertEquals(
                "Inference resource reading timed out",
                read.await().exceptionOrNull()?.message
            )
            assertTrue(session.generation.isActive)
            assertFalse(session.commands.any { it.what == InferenceProtocol.CANCEL || it.what == InferenceProtocol.EXIT })
        }

    @Test
    fun `given inference telemetry, late response follows timeout, expected only new request can complete next reading`() =
        runTest {
            // Given
            val session = connect()
            val oldRead =
                backgroundScope.async { runCatching { session.connection.readPerformance() } }
            pump()
            val oldRequest = session.requestId()
            advanceTimeBy(2_000)
            pump()
            assertTrue(oldRead.await().isFailure)
            val nextRead = backgroundScope.async { session.connection.readPerformance() }
            pump()
            val nextRequest = session.requestId()

            // When
            session.respond(oldRequest)
            pump()
            val hasAcceptedStaleReply = nextRead.isCompleted
            session.respond(nextRequest)
            pump()

            // Then
            assertFalse(hasAcceptedStaleReply)
            assertTrue(nextRequest > oldRequest)
            assertEquals(400L, nextRead.await()?.residentBytes)
        }

    @Test
    fun `given inference telemetry, untrusted or stale envelopes arrive, expected pending reading accepts only validated reply`() =
        runTest {
            // Given
            val session = connect()
            val read = backgroundScope.async { session.connection.readPerformance() }
            pump()
            val request = session.requestId()
            val invalidEnvelopes: List<Message.() -> Unit> = listOf(
                { sendingUid = Process.myUid() + 1 },
                { data.putInt(InferenceProtocol.VERSION_KEY, InferenceProtocol.VERSION - 1) },
                { data.putString(InferenceProtocol.INCARNATION, "wrong") },
                { data.putString(InferenceProtocol.JOB, "other-job") },
                { data.putString(InferenceProtocol.ENGINE_KEY, NativeGenerationEngine.ONNX.name) },
                { data.putLong(InferenceProtocol.SEQUENCE, 0) },
                { data.putLong(InferenceProtocol.RESOURCE_REQUEST_ID, request + 1) },
            )

            // When
            val acceptedInvalidReply = invalidEnvelopes.map { mutate ->
                session.respond(request, mutate)
                pump()
                read.isCompleted
            }
            session.respond(request)
            pump()

            // Then
            assertTrue(acceptedInvalidReply.none { it })
            assertEquals(200L, read.await()?.cpuTimeNanos)
        }

    @Test
    fun `given inference telemetry, callers read concurrently, expected serialized correlated resource requests`() =
        runTest {
            // Given
            val session = connect()
            val first = backgroundScope.async { session.connection.readPerformance() }
            val second = backgroundScope.async { session.connection.readPerformance() }
            pump()
            val initialRequests =
                session.commands.count { it.what == InferenceProtocol.READ_PERFORMANCE }
            val firstId = session.requestId()

            // When
            session.respond(firstId)
            pump()
            val secondId = session.requestId()
            session.respond(secondId) { data.putLong(InferenceProtocol.RESIDENT_BYTES, 800) }
            pump()

            // Then
            assertEquals(1, initialRequests)
            assertEquals(
                2,
                session.commands.count { it.what == InferenceProtocol.READ_PERFORMANCE })
            assertTrue(secondId > firstId)
            assertEquals(400L, first.await()?.residentBytes)
            assertEquals(800L, second.await()?.residentBytes)
        }

    @Test
    fun `given inference telemetry, collector cancels resource read, expected later reading works and inference stays active`() =
        runTest {
            // Given
            val session = connect()
            val first = backgroundScope.async { session.connection.readPerformance() }
            pump()

            // When
            first.cancelAndJoin()
            val second = backgroundScope.async { session.connection.readPerformance() }
            pump()
            session.respond(session.requestId())
            pump()

            // Then
            assertEquals(400L, second.await()?.residentBytes)
            assertTrue(session.generation.isActive)
            assertFalse(session.commands.any { it.what == InferenceProtocol.CANCEL })
        }

    @Test
    fun `given inference telemetry, child disconnects during read, expected pending read fails promptly and generation fails`() =
        runTest {
            // Given
            val session = connect()
            val read =
                backgroundScope.async { runCatching { session.connection.readPerformance() } }
            pump()

            // When
            session.disconnect()
            pump()

            // Then
            assertEquals("Inference process exited", read.await().exceptionOrNull()?.message)
            assertTrue(session.generation.await().isFailure)
        }

    @Test
    fun `given inference telemetry, generation finishes, expected later reading absent without further resource request`() =
        runTest {
            // Given
            val session = connect()
            session.send(InferenceProtocol.SUCCESS)
            pump()

            // When
            val reading = session.connection.readPerformance()

            // Then
            assertNull(reading)
            assertTrue(session.generation.await().isSuccess)
            assertFalse(session.commands.any { it.what == InferenceProtocol.READ_PERFORMANCE })
        }

    private fun TestScope.connect(): Session {
        val session = Session().also(sessions::add)
        session.generation = backgroundScope.async {
            runCatching {
                session.connection.run(
                    session.job,
                    NativeGenerationEngine.MNN,
                    {},
                    {},
                    { _, _ -> })
            }
        }
        runCurrent()
        session.attachPeer()
        session.send(InferenceProtocol.READY)
        pump()
        assertTrue(
            "Expected submitted job, received ${session.commands.map { it.what }}",
            session.commands.any { it.what == InferenceProtocol.SUBMIT })
        // The production heartbeat uses wall-clock uptime; this fixture advances only virtual
        // coroutine time to test the independent resource deadline.
        ReflectionHelpers.getField<Job>(session.connection, "heartbeat").cancel()
        return session
    }

    private fun TestScope.pump() {
        runCurrent()
    }

    private class Session {
        val incarnation = "a7f4298e-4dfa-4b00-b49a-ec6d6e326fbb"
        val job = "25c57c00-f42d-4f07-a401-8d1b88d7528d"
        val component = ComponentName("test", "Inference")
        val binding = slot<ServiceConnection>()
        val commands = mutableListOf<Message>()
        private val context = mockk<Context>(relaxed = true) {
            every { bindService(any(), capture(binding), any<Int>()) } returns true
        }
        val connection = AndroidInferenceConnection(context)
        lateinit var generation: Deferred<Result<Unit>>
        private var sequence = 0L
        private val peer = mockk<Messenger> {
            every { send(any()) } answers { commands.add(firstArg()); Unit }
        }

        fun attachPeer() {
            ReflectionHelpers.setField(connection, "remote", peer)
        }

        fun requestId() = commands.last { it.what == InferenceProtocol.READ_PERFORMANCE }
            .data.getLong(InferenceProtocol.RESOURCE_REQUEST_ID)

        fun respond(requestId: Long, mutate: Message.() -> Unit = {}) {
            send(InferenceProtocol.PERFORMANCE) {
                data.putLong(InferenceProtocol.RESOURCE_REQUEST_ID, requestId)
                data.putLong(InferenceProtocol.CPU_NANOS, 200)
                data.putLong(InferenceProtocol.RESIDENT_BYTES, 400)
                mutate()
            }
        }

        fun send(what: Int, mutate: Message.() -> Unit = {}) {
            val message = InferenceProtocol.message(what, incarnation, job, ++sequence).apply {
                sendingUid = Process.myUid()
                data.putInt(InferenceProtocol.PID, 99)
                data.putString(InferenceProtocol.ENGINE_KEY, NativeGenerationEngine.MNN.name)
                mutate()
            }
            ReflectionHelpers.callInstanceMethod<Boolean>(
                connection,
                "onMessage",
                ClassParameter.from(Message::class.java, message)
            )
        }

        fun disconnect() {
            if (binding.isCaptured) binding.captured.onServiceDisconnected(component)
        }
    }
}
