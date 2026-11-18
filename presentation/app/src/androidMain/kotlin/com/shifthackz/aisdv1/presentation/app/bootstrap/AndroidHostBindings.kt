package com.shifthackz.aisdv1.presentation.app.bootstrap

import com.shifthackz.aisdv1.core.common.appbuild.ActivityIntentProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import com.shifthackz.aisdv1.domain.feature.sdxl.StableDiffusionCpp

/**
 * Android host collaborators resolved lazily after graph registration.
 *
 * @author Dmitriy Moroz
 */
interface AndroidHostBindings {
    /** External destinations selected by the installed Android build. */
    val linksProvider: LinksProvider

    /** Package version, native platform and distribution flavor. */
    val buildInfoProvider: BuildInfoProvider

    /** Dispatchers preserving Android main-immediate UI delivery. */
    val dispatchersProvider: DispatchersProvider

    /** Native wall and monotonic clocks used by shared operations. */
    val timeProvider: TimeProvider

    /** App-owned filesystem roots and the manifest file-provider authority. */
    val fileProviderDescriptor: FileProviderDescriptor

    /** Creates launcher intents retaining the installed Activity identity. */
    val activityIntentProvider: ActivityIntentProvider

    /** Host transport to isolated inference; resolving it never loads host JNI. */
    val nativeGenerationGateway: NativeGenerationGateway

    /** Reads previous process exits for generation attempt diagnostics. */
    val processExitDiagnosticsGateway: ProcessExitDiagnosticsGateway

    /** SDCPP adapter that routes native generation into the isolated child process. */
    val stableDiffusionCpp: StableDiffusionCpp
}
