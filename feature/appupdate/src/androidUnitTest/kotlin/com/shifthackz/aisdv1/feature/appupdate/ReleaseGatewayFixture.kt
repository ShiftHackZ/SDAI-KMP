package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildVersion
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json

/**
 * Exercises the production gateway and decoder with typed upstream envelopes and captured requests.
 * Each fixture owns its HTTP clients and closes them after its test.
 *
 * @author Dmitriy Moroz
 */
internal class ReleaseGatewayFixture {
    val requests = mutableListOf<HttpRequestData>()
    private val clients = mutableListOf<HttpClient>()

    /**
     * Creates production discovery backed by an isolated transport response.
     *
     * @param payload Response body delivered to the real bounded reader and decoder.
     * @param version Installed marketing version, including the nightly suffix when applicable.
     * @param buildNumber Installed monotonic version code.
     * @param isAppStore Whether the installation is distributed by Apple.
     * @param status HTTP result, allowing strict upstream failure checks.
     * @param failure Optional transport failure raised before a response is delivered.
     * @author Dmitriy Moroz
     */
    fun gateway(
        payload: String,
        version: String = "1.2.0",
        buildNumber: Int = 100,
        isAppStore: Boolean = false,
        status: HttpStatusCode = HttpStatusCode.OK,
        failure: Throwable? = null,
    ): ReleaseAppUpdateGateway {
        val client = HttpClient(MockEngine { request ->
            requests += request
            failure?.let { throw it }
            respond(payload, status, headersOf("Content-Type", "application/json"))
        }) { configureAppUpdateClient() }
        clients += client
        val build = object : BuildInfoProvider by BuildInfoProvider.stub {
            override val version = BuildVersion(version)
            override val buildNumber = buildNumber
        }
        return ReleaseAppUpdateGateway(client, build, isAppStore)
    }

    /**
     * Closes every transport created by the current test.
     *
     * @author Dmitriy Moroz
     */
    fun close() {
        clients.forEach(HttpClient::close)
    }
}

/**
 * Encodes the GitHub fields consumed by production update discovery.
 *
 * @param tag Release tag, stable or the moving nightly identifier.
 * @param isDraft Whether the release is unpublished.
 * @param isPrerelease Whether GitHub classifies it outside the stable channel.
 * @param body CI release notes; null also exercises stable releases without notes.
 * @param assets Names of distributable release attachments.
 * @author Dmitriy Moroz
 */
internal fun releasePayload(
    tag: String = "1.3.0",
    isDraft: Boolean = false,
    isPrerelease: Boolean = false,
    body: String? = null,
    assets: List<String> = emptyList(),
): String =
    Json.encodeToString(GitHubRelease(tag, isDraft, isPrerelease, body, assets.map(::GitHubAsset)))

/**
 * Encodes Apple's public application lookup using the production response models.
 *
 * @param version Published marketing version.
 * @param trackId Application identity returned by the store.
 * @author Dmitriy Moroz
 */
internal fun appStorePayload(version: String = "1.3.0", trackId: Long = 6778314183L): String =
    Json.encodeToString(AppStoreLookup(listOf(AppStoreRelease(trackId, version))))
