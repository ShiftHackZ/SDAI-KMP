package com.shifthackz.aisdv1.data.preference

import android.content.Context
import com.shifthackz.aisdv1.domain.entity.ServerSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Exercises production preference setters and checked Android storage across owner reopening.
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EasyDiffusionSelectionPersistenceTest {
    private val suite = "easy-diffusion-selection-test"
    private lateinit var preferences: PreferenceManagerImpl

    @Before
    fun openCleanStorage() {
        val context: Context = RuntimeEnvironment.getApplication()
        check(context.getSharedPreferences(suite, Context.MODE_PRIVATE).edit().clear().commit())
        startKoin { modules(module { single<Context> { context } }) }
        preferences = PreferenceManagerImpl(createKeyValueStore(suite))
    }

    @After
    fun closeDependencies() {
        stopKoin()
    }

    @Test
    fun `selected endpoint and checkpoint changes advance durable selection across reopening`() {
        preferences.source = ServerSource.EASY_DIFFUSION
        val initial = preferences.providerSelectionRevision
        preferences.easyDiffusionUrl = "https://server.example"
        assertEquals(initial + 1, preferences.providerSelectionRevision)
        preferences.easyDiffusionModel = "folder/model"
        assertEquals(initial + 2, preferences.providerSelectionRevision)
        val reopened = PreferenceManagerImpl(createKeyValueStore(suite))
        assertEquals(ServerSource.EASY_DIFFUSION, reopened.source)
        assertEquals(initial + 2, reopened.providerSelectionRevision)
        assertEquals("https://server.example", reopened.easyDiffusionUrl)
        assertEquals("folder/model", reopened.easyDiffusionModel)
    }

    @Test
    fun `same endpoint checkpoint and provider preserve revision`() {
        preferences.source = ServerSource.EASY_DIFFUSION
        preferences.easyDiffusionUrl = "https://server.example"
        preferences.easyDiffusionModel = "folder/model"
        val revision = preferences.providerSelectionRevision
        preferences.source = ServerSource.EASY_DIFFUSION
        preferences.easyDiffusionUrl = "https://server.example"
        preferences.easyDiffusionModel = "folder/model"
        assertEquals(revision, preferences.providerSelectionRevision)
        assertEquals(
            revision,
            PreferenceManagerImpl(createKeyValueStore(suite)).providerSelectionRevision
        )
    }

    @Test
    fun `inactive provider settings persist without invalidating selected provider`() {
        preferences.source = ServerSource.AUTOMATIC1111
        val revision = preferences.providerSelectionRevision
        preferences.easyDiffusionUrl = "https://server.example"
        preferences.easyDiffusionModel = "folder/model"
        val reopened = PreferenceManagerImpl(createKeyValueStore(suite))
        assertEquals(ServerSource.AUTOMATIC1111, reopened.source)
        assertEquals(revision, reopened.providerSelectionRevision)
        assertEquals("folder/model", reopened.easyDiffusionModel)
    }

    @Test
    fun `away and back selection invalidates earlier work even when endpoint is unchanged`() {
        preferences.source = ServerSource.EASY_DIFFUSION
        val revision = preferences.providerSelectionRevision
        preferences.source = ServerSource.SWARM_UI
        preferences.source = ServerSource.EASY_DIFFUSION
        assertEquals(
            revision + 2,
            PreferenceManagerImpl(createKeyValueStore(suite)).providerSelectionRevision
        )
    }
}
