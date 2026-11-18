package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget

/**
 * Creates the remembered native picker for explicit image-to-image input selection.
 * Conversion and normalization use background execution; cancellation and unsupported picker
 * outcomes remain distinct from invalid or rejected image content.
 *
 * @param budget Encoded/pixel admission for native picker conversion.
 *
 * @return Native input actions using the composition root's admission budget.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun rememberImageToImagePlatformActions(budget: OriginalImageBudget): ImageToImagePlatformActions
