package com.shifthackz.aisdv1.app

import android.content.Intent
import android.content.Context
import com.shifthackz.aisdv1.presentation.app.bootstrap.AndroidHostBindings
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import com.shifthackz.aisdv1.domain.feature.sdxl.StableDiffusionCpp
import com.shifthackz.aisdv1.app.inference.AndroidInferenceConnectionFactory
import com.shifthackz.aisdv1.app.inference.CompletePngImageValidator
import com.shifthackz.aisdv1.app.inference.FileGenerationFileStore
import com.shifthackz.aisdv1.app.inference.NativeGenerationGatewayImpl
import com.shifthackz.aisdv1.app.inference.RemoteStableDiffusionCpp
import com.shifthackz.aisdv1.app.diagnostics.AndroidProcessExitDiagnosticsGateway
import com.shifthackz.aisdv1.app.diagnostics.FileGenerationAttemptJournal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.appbuild.BuildVersion
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.links.DefaultLinksProvider
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.presentation.activity.AiStableDiffusionActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Supplies native configuration and lazy process transport to the shared composition root.
 * @param context Native application context.
 * @author Dmitriy Moroz
 */
internal class NativeHostBindings(private val context: Context) : AndroidHostBindings {

    override val linksProvider: LinksProvider by lazy {
        object : LinksProvider {
            override val hordeUrl: String = BuildConfig.HORDE_AI_URL
            override val hordeSignUpUrl: String = BuildConfig.HORDE_AI_SIGN_UP_URL
            override val huggingFaceUrl: String = BuildConfig.HUGGING_FACE_INFO_URL
            override val openAiInfoUrl: String = BuildConfig.OPEN_AI_INFO_URL
            override val stabilityAiInfoUrl: String = BuildConfig.STABILITY_AI_INFO_URL
            override val falAiInfoUrl: String = BuildConfig.FAL_AI_INFO_URL
            override val arliAiInfoUrl: String = BuildConfig.ARLI_AI_INFO_URL
            override val privacyPolicyUrl: String = BuildConfig.POLICY_URL
            override val donateUrl: String = BuildConfig.DONATE_URL
            override val projectWebsiteUrl: String = BuildConfig.PROJECT_WEBSITE_URL
            override val developerWebsiteUrl: String = BuildConfig.DEVELOPER_WEBSITE_URL
            override val gitHubSourceUrl: String = BuildConfig.GITHUB_SOURCE_URL
            override val licenseUrl: String = BuildConfig.LICENSE_URL
            override val setupInstructionsUrl: String = BuildConfig.SETUP_INSTRUCTIONS_URL
            override val swarmUiInfoUrl: String = BuildConfig.SWARM_UI_INFO_URL
            override val demoModeUrl: String = BuildConfig.DEMO_MODE_API_URL
            override val telegramCommunityUrl: String = "https://t.me/sdai_app"
            override val discordCommunityUrl: String = "https://discord.gg/jzdR9m8Ves"
            override val linkedInCommunityUrl: String = DefaultLinksProvider.linkedInCommunityUrl
        }
    }

    override val buildInfoProvider: BuildInfoProvider by lazy {
        object : BuildInfoProvider {
            override val isDebug: Boolean = BuildConfig.DEBUG
            override val buildNumber: Int = BuildConfig.VERSION_CODE
            override val version: BuildVersion = BuildVersion(BuildConfig.VERSION_NAME)
            override val type: BuildType = BuildType.fromBuildConfig(BuildConfig.BUILD_FLAVOR_TYPE)
            override val platform: Platform = Platform.ANDROID

            override fun toString(): String = buildString {
                append("$version")
                if (BuildConfig.DEBUG) append("-dev")
                append(" ($buildNumber)")
                when (type) {
                    BuildType.FULL -> append(" FULL")
                    BuildType.FOSS -> append(" FOSS")
                    BuildType.PLAY -> Unit
                }
            }
        }
    }

    override val dispatchersProvider: DispatchersProvider by lazy {
        object : DispatchersProvider {
            override val io: CoroutineDispatcher = Dispatchers.IO
            override val ui: CoroutineDispatcher = Dispatchers.Main
            override val immediate: CoroutineDispatcher = Dispatchers.Main.immediate
        }
    }

    override val timeProvider: TimeProvider by lazy {
        object : TimeProvider {
            override fun nanoTime(): Long = System.nanoTime()
            override fun currentTimeMillis(): Long = System.currentTimeMillis()
        }
    }

    override val fileProviderDescriptor: FileProviderDescriptor by lazy {
        object : FileProviderDescriptor {
            override val providerPath: String = "${context.packageName}.fileprovider"
            override val imagesCacheDirPath: String = "${context.cacheDir}/images"
            override val logsCacheDirPath: String = "${context.cacheDir}/logs"
            override val localModelDirPath: String =
                "${context.filesDir.absolutePath}/model"
            override val workCacheDirPath: String = "${context.cacheDir}/work"
        }
    }

    override val activityIntentProvider: ActivityIntentProvider by lazy {
        ActivityIntentProvider {
            Intent(context, AiStableDiffusionActivity::class.java)
        }
    }
    private val files by lazy { FileGenerationFileStore(context, CompletePngImageValidator()) }
    override val nativeGenerationGateway: NativeGenerationGateway by lazy {
        NativeGenerationGatewayImpl(
            AndroidInferenceConnectionFactory(context),
            files,
            FileGenerationAttemptJournal(context),
            processExitDiagnosticsGateway,
            CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }
    override val processExitDiagnosticsGateway: ProcessExitDiagnosticsGateway by lazy {
        AndroidProcessExitDiagnosticsGateway(
            context
        )
    }
    override val stableDiffusionCpp: StableDiffusionCpp by lazy {
        RemoteStableDiffusionCpp(
            nativeGenerationGateway
        )
    }
}
