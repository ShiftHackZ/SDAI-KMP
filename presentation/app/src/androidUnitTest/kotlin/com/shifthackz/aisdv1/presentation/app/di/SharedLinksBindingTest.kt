package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.core.common.links.DefaultLinksProvider
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import org.junit.Assert.assertSame
import org.junit.Test
import org.koin.dsl.koinApplication

class SharedLinksBindingTest {
    @Test
    fun `given shared application bindings, links are resolved on Android, expected common provider without a native override`() {
        // Given
        val application = koinApplication { modules(appCoreModule) }
        try {
            // When
            val links = application.koin.get<LinksProvider>()
            // Then
            assertSame(DefaultLinksProvider, links)
        } finally {
            application.close()
        }
    }
}
