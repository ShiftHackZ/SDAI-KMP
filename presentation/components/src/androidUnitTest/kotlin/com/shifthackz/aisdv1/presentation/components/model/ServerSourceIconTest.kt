package com.shifthackz.aisdv1.presentation.components.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.components.widget.icon.BrandIcons
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Protects local provider branding in shared configuration and generation controls.
 * @author Dmitriy Moroz
 */
class ServerSourceIconTest {
    @Test
    fun `given local providers, icon is requested on iOS, expected Apple branding for every local engine`() {
        // Given
        val providers = ServerSource.entries.filter { it.type == ServerSourceType.LOCAL }
        // When
        val icons = providers.map { it.providerIcon(Platform.IOS) }
        // Then
        icons.forEach {
            assertEquals(BrandIcons.Apple, it); assertNotEquals(
            Icons.Default.Android,
            it
        )
        }
    }

    @Test
    fun `given local providers, icon is requested on Android, expected Android branding for every local engine`() {
        // Given
        val providers = ServerSource.entries.filter { it.type == ServerSourceType.LOCAL }
        // When
        val icons = providers.map { it.providerIcon(Platform.ANDROID) }
        // Then
        icons.forEach { assertEquals(Icons.Default.Android, it) }
    }
}
