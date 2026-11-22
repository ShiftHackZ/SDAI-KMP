package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.domain.di.coreDomainModule
import com.shifthackz.aisdv1.feature.appupdate.di.appUpdateModule
import com.shifthackz.aisdv1.domain.gateway.AppUpdateGateway
import com.shifthackz.aisdv1.domain.usecase.appupdate.CheckAppUpdateUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.koin.dsl.koinApplication

/**
 * Ensures the FOSS composition root remains inert without an Android context or network client.
 *
 * @author Dmitriy Moroz
 */
class FossAppUpdateModuleTest {
    @Test
    fun `given FOSS distribution DI, launch discovery is resolved and invoked, expected the NoOp gateway and no offer`() =
        runBlocking {
            // Given
            val application = koinApplication { modules(coreDomainModule, appUpdateModule) }
            try {
                val gateway = application.koin.get<AppUpdateGateway>()
                val check = application.koin.get<CheckAppUpdateUseCase>()

                // When
                val result = check()

                // Then
                assertSame(NoOpAppUpdateGateway, gateway)
                assertNull(result)
            } finally {
                application.close()
            }
        }
}
