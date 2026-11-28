package com.shifthackz.aisdv1.presentation.report

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ReportReason
import com.shifthackz.aisdv1.presentation.components.widget.image.SampledBase64Image
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.textFieldColors
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar

/**
 * Report image, editable fields and submission status consumed by the stateless renderer.
 *
 * @param loading Whether image loading or submission progress should be shown.
 * @param imageBase64 Original encoded generation image used for the report preview.
 * @param text Current user-entered report description.
 * @param reason Current report category.
 * @param reportSent Whether to show submission completion controls.
 * @author Dmitriy Moroz
 */
@Immutable
data class ReportScreenContentState(
    val loading: Boolean = true,
    val imageBase64: String = "",
    val text: String = "",
    val reason: ReportReason = ReportReason.Other,
    val reportSent: Boolean = false,
)

/**
 * Localized report actions, explanatory text, category names and accessibility labels.
 *
 * @param title Report toolbar heading.
 * @param done Completion button text.
 * @param submit Submission action text.
 * @param sent Submission-complete message.
 * @param description Caption for the free-text report description.
 * @param backContentDescription Accessible back-navigation label.
 * @param sendContentDescription Accessible submit-action label.
 * @param doneContentDescription Accessible completion-action label.
 * @param imageContentDescription Accessible description of the reported image.
 * @param reasonInappropriateContent Label for inappropriate-content reports.
 * @param reasonViolence Label for violent-content reports.
 * @param reasonHatefulSpeech Label for hateful-speech reports.
 * @param reasonIntellectualProperty Label for intellectual-property reports.
 * @param reasonAdultContent Label for adult-content reports.
 * @param reasonOther Label for other report categories.
 * @author Dmitriy Moroz
 */
data class ReportScreenStrings(
    val title: String,
    val done: String,
    val submit: String,
    val sent: String,
    val description: String,
    val backContentDescription: String,
    val sendContentDescription: String,
    val doneContentDescription: String,
    val imageContentDescription: String,
    val reasonInappropriateContent: String,
    val reasonViolence: String,
    val reasonHatefulSpeech: String,
    val reasonIntellectualProperty: String,
    val reasonAdultContent: String,
    val reasonOther: String,
)

/**
 * Renders report editing, image preview and submission-complete feedback from caller-owned state.
 *
 * @param state Current report image, editable fields and submission status.
 * @param strings Localized labels, category names and accessibility text.
 * @param modifier Layout modifiers for the report scaffold.
 * @param onNavigateBack Requests back navigation or dismissal after completion.
 * @param onSubmit Requests submission of the current fields.
 * @param onTextChange Reports a new user-entered report description.
 * @param onReasonChange Reports a newly selected report category.
 * @param imageContent Image renderer for the caller-sized preview slot; defaults to bounded Base64 sampling.
 * @author Dmitriy Moroz
 */
@Composable
fun ReportScreenContent(
    state: ReportScreenContentState,
    strings: ReportScreenStrings,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit,
    onTextChange: (String) -> Unit,
    onReasonChange: (ReportReason) -> Unit,
    modifier: Modifier = Modifier,
    imageContent: @Composable (Modifier) -> Unit = { imageModifier ->
        SampledBase64Image(
            modifier = imageModifier,
            base64 = state.imageBase64,
            contentScale = ContentScale.Crop,
            contentDescription = strings.imageContentDescription,
        )
    },
) {
    PlatformScaffold(
        canUnderlapToolbar = true,
        modifier = modifier.imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(text = strings.title)
                },
                navigationIcon = {
                    ToolbarIconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.backContentDescription,
                        )
                    }
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
        bottomBar = {
            PrimaryButton(
                modifier = Modifier
                    .windowInsetsPadding(persistentBottomBarWindowInsets())
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                onClick = {
                    if (state.reportSent) onNavigateBack() else onSubmit()
                },
                isEnabled = !state.loading,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = if (state.reportSent) {
                        Icons.Default.Check
                    } else {
                        Icons.AutoMirrored.Filled.Send
                    },
                    contentDescription = strings.sendContentDescription,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = if (state.reportSent) strings.done else strings.submit,
                    color = LocalContentColor.current,
                )
            }
        },
    ) { paddingValues ->
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            targetState = !state.loading,
            label = "report_state_animator",
        ) { contentVisible ->
            if (contentVisible) {
                ReportLoadedContent(
                    state = state,
                    strings = strings,
                    onTextChange = onTextChange,
                    onReasonChange = onReasonChange,
                    imageContent = imageContent,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(60.dp)
                            .aspectRatio(1f),
                    )
                }
            }
        }
    }
}

/**
 * Shows editable report fields or the submitted confirmation within the scrollable content area.
 *
 * @param state Image presence, draft values and completion flag owned by the caller.
 * @param strings Localized field, category and confirmation labels.
 * @param onTextChange Receives edits to the report description.
 * @param onReasonChange Receives selection of a report category.
 * @param modifier Placement and external decoration of the scrollable report body.
 * @param imageContent Renders the reported image inside the measured square slot.
 * @author Dmitriy Moroz
 */
@Composable
private fun ReportLoadedContent(
    state: ReportScreenContentState,
    strings: ReportScreenStrings,
    onTextChange: (String) -> Unit,
    onReasonChange: (ReportReason) -> Unit,
    modifier: Modifier = Modifier,
    imageContent: @Composable (Modifier) -> Unit,
) {
    val scrollState = rememberScrollState()
    ToolbarScrollObserver(scrollState.canScrollBackward)
    Column(
        modifier = modifier
            .verticalScrollbar(scrollState, topInset = LocalToolbarContentInset.current)
            .verticalScroll(scrollState)
            .padding(top = LocalToolbarContentInset.current)
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.reportSent) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(100.dp),
                imageVector = Icons.Default.CheckCircle,
                contentDescription = strings.doneContentDescription,
            )
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                text = strings.sent,
                style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            if (state.imageBase64.isNotBlank()) {
                imageContent(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.45f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReportReason.entries.forEach { reason ->
                    FilterChip(
                        selected = reason == state.reason,
                        onClick = { onReasonChange(reason) },
                        label = {
                            Text(text = strings.label(reason))
                        },
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = state.text,
                onValueChange = onTextChange,
                label = {
                    Text(text = strings.description)
                },
                colors = textFieldColors,
            )
        }
    }
}

private fun ReportScreenStrings.label(reason: ReportReason): String = when (reason) {
    ReportReason.InappropriateContent -> reasonInappropriateContent
    ReportReason.Violence -> reasonViolence
    ReportReason.HatefulSpeech -> reasonHatefulSpeech
    ReportReason.IntellectualPropertyInfringement -> reasonIntellectualProperty
    ReportReason.AdultContent -> reasonAdultContent
    ReportReason.Other -> reasonOther
}
