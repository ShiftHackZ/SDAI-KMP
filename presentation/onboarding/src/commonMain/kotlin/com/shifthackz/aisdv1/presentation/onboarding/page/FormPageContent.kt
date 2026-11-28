package com.shifthackz.aisdv1.presentation.onboarding.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.extensions.gesturesDisabled
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.onboarding.buildOnBoardingText
import com.shifthackz.aisdv1.presentation.onboarding.onBoardingPhoneWidthFraction
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageContent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingDeviceFrame

/**
 * Shows the noninteractive tagged text-to-image form with sample prompts and fixed generation settings.
 * A gesture-blocking overlay prevents the preview from changing application state.
 *
 * @param modifier Compose modifier applied to the rendered UI.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FormPageContent(
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = buildOnBoardingText(Localization.string("on_boarding_page_form_title")),
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.weight(1f))
    OnBoardingDeviceFrame(
        modifier = Modifier.fillMaxWidth(onBoardingPhoneWidthFraction),
    ) { previewModifier ->

        TextToImageContent(
            modifier = previewModifier,
            state = TextToImageState(
                loadingConfiguration = false,
                onBoardingDemo = true,
                formPromptTaggedInput = true,
                prompt = "man, photorealistic, black hair, aviator glasses, handsome, beautiful, nature background",
                negativePrompt = "bad anatomy, bad fingers, distorted, jpeg artifacts",
                width = "512",
                height = "512",
                samplingSteps = 24,
                cfgScale = 7.5f,
                batchCount = 2,
            ),
            processIntent = {},
            backgroundWorkContent = {},
        )
        Box(
            modifier = previewModifier
                .gesturesDisabled(),
        )

    }
    Spacer(modifier = Modifier.weight(1f))
}
