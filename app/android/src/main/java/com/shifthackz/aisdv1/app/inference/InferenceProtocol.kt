package com.shifthackz.aisdv1.app.inference

import android.os.Bundle
import android.os.Message
import android.os.Messenger

/**
 * Small, versioned control messages. Sensitive request data and image bytes live only in files.
 *
 * @author Dmitriy Moroz
 */
internal object InferenceProtocol {
    const val VERSION = 2
    const val HELLO = 1
    const val READY = 2
    const val SUBMIT = 3
    const val PROGRESS = 4
    const val SUCCESS = 5
    const val FAILURE = 6
    const val CANCEL = 7
    const val PING = 8
    const val PONG = 9
    const val EXIT = 10
    const val RESOURCE_REQUEST_ID = "resource_request_id"
    const val READ_PERFORMANCE = 12
    const val PERFORMANCE = 13
    const val CPU_NANOS = "cpu_nanos"
    const val RESIDENT_BYTES = "resident_bytes"
    const val STAGE = 11
    const val STAGE_KEY = "stage"
    const val ENGINE_KEY = "engine"
    const val VERSION_KEY = "version"
    const val INCARNATION = "incarnation"
    const val JOB = "job"
    const val SEQUENCE = "sequence"
    const val CURRENT = "current"
    const val TOTAL = "total"
    const val PID = "pid"

    /**
     * Builds a bounded envelope. Receiver validates version, incarnation and sequence.
     *
     * @param what Protocol operation.
     * @param incarnation Random child instance identity.
     * @param job Opaque UUID, empty for handshake and heartbeat.
     * @param sequence Monotonically increasing child event number.
     * @param replyTo Host callback binder, set only by the host.
     * @author Dmitriy Moroz
     */
    fun message(
        what: Int,
        incarnation: String = "",
        job: String = "",
        sequence: Long = 0,
        replyTo: Messenger? = null
    ): Message =
        Message.obtain(null, what).apply {
            data = Bundle().apply {
                putInt(VERSION_KEY, VERSION)
                putString(INCARNATION, incarnation)
                putString(JOB, job)
                putLong(SEQUENCE, sequence)
            }
            this.replyTo = replyTo
        }
}
