package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File
import kotlin.coroutines.resume

/**
 * Creates the remembered native picker for explicit image-to-image input selection.
 * Conversion and normalization use background execution; cancellation and unsupported picker
 * outcomes remain distinct from invalid or rejected image content.
 *
 * @param budget Encoded/pixel admission for native picker conversion.
 * @return Native input actions using the composition root's admission budget.
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberImageToImagePlatformActions(budget: OriginalImageBudget): ImageToImagePlatformActions {
    val context = LocalContext.current
    val fileProviderDescriptor = koinInject<FileProviderDescriptor>()
    val pickerState = remember { AndroidImagePickerState() }
    val cameraFile = remember(context) {
        File(context.cacheDir, "img2img-camera.jpg").apply {
            createNewFile()
        }
    }
    val cameraUri = remember(context, fileProviderDescriptor, cameraFile) {
        FileProvider.getUriForFile(
            context,
            fileProviderDescriptor.providerPath,
            cameraFile,
        )
    }

    val cameraPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            pickerState.resume(AndroidImagePickOutcome.Selected(cameraUri))
        } else {
            pickerState.resume(AndroidImagePickOutcome.Cancelled)
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            cameraPicker.launch(cameraUri)
        } else {
            pickerState.resume(AndroidImagePickOutcome.Failed("Camera permission was not granted"))
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            pickerState.resume(AndroidImagePickOutcome.Cancelled)
        } else {
            pickerState.resume(AndroidImagePickOutcome.Selected(uri))
        }
    }

    return remember(context, pickerState, cameraPicker, cameraPermission, mediaPicker, budget) {
        object : ImageToImagePlatformActions {
            override suspend fun pickImage(source: ImageToImagePickSource): ImageToImagePickResult =
                when (val outcome = pickerState.await {
                    when (source) {
                        ImageToImagePickSource.Camera -> {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA,
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraPicker.launch(cameraUri)
                            } else {
                                cameraPermission.launch(Manifest.permission.CAMERA)
                            }
                        }

                        ImageToImagePickSource.Gallery -> {
                            val request = PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            )
                            mediaPicker.launch(request)
                        }
                    }
                }) {
                    AndroidImagePickOutcome.Cancelled -> ImageToImagePickResult.Cancelled
                    is AndroidImagePickOutcome.Failed -> ImageToImagePickResult.Failed(outcome.message)
                    is AndroidImagePickOutcome.Selected -> withContext(Dispatchers.Default) {
                        context.imageUriToPickResult(outcome.uri, budget)
                    }
                }
        }
    }
}


private sealed interface AndroidImagePickOutcome {

    data class Selected(val uri: Uri) : AndroidImagePickOutcome


    data object Cancelled : AndroidImagePickOutcome


    data class Failed(val message: String) : AndroidImagePickOutcome
}

/**
 * Owns at most one waiting Activity-result consumer; cancellation detaches its continuation without reusing it.
 *
 * @author Dmitriy Moroz
 */
private class AndroidImagePickerState {

    private var continuation: CancellableContinuation<AndroidImagePickOutcome>? = null

    /**
     * Suspends for one picker result and rejects overlapping acquisitions.
     *
     * @param launchPicker Launches the Activity-owned picker or permission request after the consumer is installed.
     * @return Native selection, user cancellation or acquisition failure.
     *
     * @author Dmitriy Moroz
     */
    suspend fun await(launchPicker: () -> Unit): AndroidImagePickOutcome =
        suspendCancellableCoroutine { nextContinuation ->
            if (continuation != null) {
                nextContinuation.resume(
                    AndroidImagePickOutcome.Failed("Another image picker is already active"),
                )
                return@suspendCancellableCoroutine
            }
            continuation = nextContinuation
            nextContinuation.invokeOnCancellation {
                if (continuation == nextContinuation) {
                    continuation = null
                }
            }
            launchPicker()
        }

    /**
     * Delivers a native result once to the current waiting consumer.
     *
     * @param result Outcome reported by the Activity-result callback.
     *
     * @author Dmitriy Moroz
     */
    fun resume(result: AndroidImagePickOutcome) {
        val current = continuation ?: return
        continuation = null
        current.resume(result)
    }
}
