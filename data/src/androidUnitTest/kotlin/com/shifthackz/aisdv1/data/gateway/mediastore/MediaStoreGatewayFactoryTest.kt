package com.shifthackz.aisdv1.data.gateway.mediastore

import android.content.Context
import android.os.Build
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolver
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

/**
 * Checks SDK-based writer selection without opening storage or decoding an image.
 *
 * @author Dmitriy Moroz
 */
class MediaStoreGatewayFactoryTest {

    private val stubContext = mockk<Context>()
    private val stubFileProviderDescriptor = mockk<FileProviderDescriptor>()

    @Test
    fun `given app running on Android SDK 26 (O), expected factory returned instance of type MediaStoreGatewayOldImpl`() {
        val factory = factory(Build.VERSION_CODES.O)
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayOldImpl)
    }

    @Test
    fun `given app running on Android SDK 31 (S), expected factory returned instance of type MediaStoreGatewayOldImpl`() {
        val factory = factory(Build.VERSION_CODES.S)
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayOldImpl)
    }

    @Test
    fun `given app running on Android SDK 32 (S_V2), expected factory returned instance of type MediaStoreGatewayImpl`() {
        val factory = factory(Build.VERSION_CODES.S_V2)
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayImpl)
    }

    @Test
    fun `given app running on Android SDK 34 (UPSIDE_DOWN_CAKE), expected factory returned instance of type MediaStoreGatewayImpl`() {
        val factory = factory(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        val actual = factory.invoke()
        Assert.assertEquals(true, actual is MediaStoreGatewayImpl)
    }

    @Test
    fun `given app running on Android SDK 36 (BAKLAVA), expected factory returned instance of type MediaStoreGatewayImpl`() {
        Assert.assertTrue(factory(Build.VERSION_CODES.BAKLAVA).invoke() is MediaStoreGatewayImpl)
    }

    /**
     * Creates the production factory with inert collaborators and an explicit SDK routing predicate.
     *
     * @param sdkInt Android API level whose writer selection the test exercises.
     * @author Dmitriy Moroz
     */
    private fun factory(sdkInt: Int) = MediaStoreGatewayFactory(
        context = stubContext,
        fileProviderDescriptor = stubFileProviderDescriptor,
        formatResolver = mockk<MediaStoreImageFormatResolver>(),
        shouldUseNewMediaStore = { sdkInt >= Build.VERSION_CODES.S_V2 },
    )
}
