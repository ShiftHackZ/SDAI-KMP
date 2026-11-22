package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildVersion
import com.shifthackz.aisdv1.domain.gateway.AppUpdateGateway
import com.shifthackz.aisdv1.domain.entity.AppUpdate
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json

/**
 * Reads public release metadata. Stable versions exclude prereleases; nightlies use the
 * version code embedded by CI because the nightly tag and publication date are reused.
 *
 * @param client Module-owned HTTP client with bounded timeouts.
 * @param build Installed flavor and version metadata.
 * @param isAppStore Whether discovery uses Apple's public storefront instead of GitHub.
 * @author Dmitriy Moroz
 */
internal class ReleaseAppUpdateGateway(
    private val client: HttpClient,
    private val build: BuildInfoProvider,
    private val isAppStore: Boolean,
) : AppUpdateGateway {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun check(): AppUpdate? {
        if (isAppStore) {
            val result =
                json.decodeFromString<AppStoreLookup>(read("https://itunes.apple.com/lookup?id=6778314183"))
                    .results.firstOrNull { it.trackId == 6778314183L } ?: return null
            if (!isNewerVersion(result.version)) return null
            return AppUpdate(result.version, "https://apps.apple.com/app/id6778314183")
        }
        val isNightly = build.version.toString().contains("-dev.")
        val endpoint = if (isNightly) "tags/nightly" else "latest"
        val release =
            json.decodeFromString<GitHubRelease>(read("https://api.github.com/repos/ShiftHackZ/SDAI-KMP/releases/$endpoint"))
        if (release.isDraft) return null
        if (isNightly) {
            if (release.tag != "nightly" || release.assets.none { it.name == "sdai-full-nightly.apk" }) return null
            val versionCode =
                Regex("""Version: `([^`]+) \(([0-9]+)\)`""").find(release.body.orEmpty())
                    ?: return null
            if ((versionCode.groupValues[2].toIntOrNull()
                    ?: return null) <= build.buildNumber
            ) return null
            return AppUpdate(
                versionCode.groupValues[1],
                "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/nightly"
            )
        }
        if (release.isPrerelease || !isNewerVersion(release.tag.removePrefix("v"))) return null
        return AppUpdate(
            release.tag,
            "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/${release.tag}"
        )
    }

    /**
     * Accepts only complete stable semantic versions and compares their numeric components.
     *
     * @param version Candidate marketing version without a tag prefix.
     * @author Dmitriy Moroz
     */
    private fun isNewerVersion(version: String): Boolean =
        Regex("""[0-9]+\.[0-9]+\.[0-9]+""").matches(version) && BuildVersion(version) > build.version

    /**
     * Retrieves bounded metadata while preserving HTTP, decoding and cancellation failures.
     *
     * @param url Fixed public distribution endpoint selected by this gateway.
     * @author Dmitriy Moroz
     */
    @OptIn(InternalAPI::class)
    private suspend fun read(url: String): String = client.prepareGet(url) {
        header("Accept", "application/json")
        header("User-Agent", "SDAI/${build.version}")
    }.execute { response ->
        val bytes = response.rawContent.readRemaining(1_048_577).readByteArray()
        check(bytes.size <= 1_048_576) { "Update metadata exceeds the size limit" }
        bytes.decodeToString()
    }
}
