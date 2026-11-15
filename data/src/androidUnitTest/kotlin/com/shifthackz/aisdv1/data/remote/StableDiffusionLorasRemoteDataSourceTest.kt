package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.mocks.mockKtorStableDiffusionLoraRaw
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111MetadataApi
import com.shifthackz.aisdv1.network.model.StableDiffusionLoraRaw
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class KtorStableDiffusionLorasRemoteDataSourceTest {

    private val stubException = Throwable("Internal server error.")
    private val stubApi = mockk<Automatic1111MetadataApi>()

    private val remoteDataSource = KtorStableDiffusionLorasRemoteDataSource(stubApi)

    @Test
    fun `2500 loras retain every name alias and path in domain models`() = runTest {
        val response = List(2_500) { index ->
            StableDiffusionLoraRaw("lora_$index", "alias_$index", "models/Lora/$index.safetensors")
        }
        coEvery { stubApi.fetchLoras(BASE_URL, null) } returns response

        val actual = remoteDataSource.fetchLoras(BASE_URL, AuthorizationCredentials.None)

        Assert.assertEquals(2_500, actual.size)
        actual.forEachIndexed { index, lora ->
            Assert.assertEquals(response[index].name, lora.name)
            Assert.assertEquals(response[index].alias, lora.alias)
            Assert.assertEquals(response[index].path, lora.path)
        }
    }

    @Test
    fun `given attempt to fetch loras, api returns success response, expected valid loras list value`() = runTest {
        coEvery {
            stubApi.fetchLoras(BASE_URL, null)
        } returns mockKtorStableDiffusionLoraRaw

        val actual = remoteDataSource.fetchLoras(BASE_URL, AuthorizationCredentials.None)

        Assert.assertEquals(mockKtorStableDiffusionLoraRaw.size, actual.size)
    }

    @Test
    fun `given attempt to fetch loras, api returns empty response, expected empty loras value`() = runTest {
        coEvery {
            stubApi.fetchLoras(BASE_URL, null)
        } returns emptyList()

        val actual = remoteDataSource.fetchLoras(BASE_URL, AuthorizationCredentials.None)

        Assert.assertEquals(emptyList<Any>(), actual)
    }

    @Test
    fun `given attempt to fetch loras, api returns error response, expected error value`() = runTest {
        coEvery {
            stubApi.fetchLoras(BASE_URL, null)
        } throws stubException

        val actual = runCatching {
            remoteDataSource.fetchLoras(BASE_URL, AuthorizationCredentials.None)
        }

        Assert.assertEquals(stubException, actual.exceptionOrNull())
    }

    private companion object {
        const val BASE_URL = "http://192.168.0.1:7860"
    }
}
