package com.shifthackz.aisdv1.data.di

import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import com.shifthackz.aisdv1.storage.db.persistent.dao.NetworkUsageDao
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/** Checks recorder installation through application DI startup before any Settings dependency is read.
 * @author Dmitriy Moroz */
class TrafficRecorderStartupTest {
    @Test
    fun `traffic recorder is ready before the first provider request`() {
        NetworkUsageCounter.recorder = null
        try {
            startKoin {
                modules(coreDataModule, module {
                    single<NetworkUsageDao> { mockk(relaxed = true) }
                })
            }
            assertNotNull(NetworkUsageCounter.recorder)
        } finally {
            stopKoin()
            NetworkUsageCounter.recorder = null
        }
    }
}
