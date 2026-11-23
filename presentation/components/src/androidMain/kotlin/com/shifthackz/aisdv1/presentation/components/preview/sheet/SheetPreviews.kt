@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.components.preview.sheet

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.presentation.components.modal.grid.GridBottomSheet
import com.shifthackz.aisdv1.presentation.components.modal.language.LanguageBottomSheet
import com.shifthackz.aisdv1.presentation.components.modal.language.LanguageFlagIcon
import com.shifthackz.aisdv1.presentation.components.modal.ldscheduler.LDSchedulerBottomSheet
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.sheet.PlatformModalBottomSheet
import com.shifthackz.aisdv1.presentation.components.widget.sheet.PlatformSheetSurface
import com.shifthackz.aisdv1.presentation.components.widget.sheet.SheetDragHandle

/**
 * Every grid, scheduler and language selection plus common sheet surfaces and drag handle.
 *
 * @author Dmitriy Moroz
 */
internal class SheetPreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("GridBottomSheet · Fixed2") {
            GridBottomSheet(currentGrid = Grid.Fixed2)
        },
        ComponentPreviewCase("GridBottomSheet · Fixed3") {
            GridBottomSheet(currentGrid = Grid.Fixed3)
        },
        ComponentPreviewCase("GridBottomSheet · Fixed4") {
            GridBottomSheet(currentGrid = Grid.Fixed4)
        },
        ComponentPreviewCase("GridBottomSheet · Fixed5") {
            GridBottomSheet(currentGrid = Grid.Fixed5)
        },
        ComponentPreviewCase("LDSchedulerBottomSheet · MAIN_THREAD") {
            LDSchedulerBottomSheet(currentScheduler = SchedulersToken.MAIN_THREAD)
        },
        ComponentPreviewCase("LDSchedulerBottomSheet · IO_THREAD") {
            LDSchedulerBottomSheet(currentScheduler = SchedulersToken.IO_THREAD)
        },
        ComponentPreviewCase("LDSchedulerBottomSheet · COMPUTATION") {
            LDSchedulerBottomSheet(currentScheduler = SchedulersToken.COMPUTATION)
        },
        ComponentPreviewCase("LDSchedulerBottomSheet · SINGLE_THREAD") {
            LDSchedulerBottomSheet(currentScheduler = SchedulersToken.SINGLE_THREAD)
        },
        ComponentPreviewCase("LanguageBottomSheet · en") {
            LanguageBottomSheet(selectedLanguageCode = "en", onLanguageSelected = {})
        },
        ComponentPreviewCase("LanguageBottomSheet · uk") {
            LanguageBottomSheet(selectedLanguageCode = "uk", onLanguageSelected = {})
        },
        ComponentPreviewCase("LanguageBottomSheet · tr") {
            LanguageBottomSheet(selectedLanguageCode = "tr", onLanguageSelected = {})
        },
        ComponentPreviewCase("LanguageBottomSheet · ru") {
            LanguageBottomSheet(selectedLanguageCode = "ru", onLanguageSelected = {})
        },
        ComponentPreviewCase("LanguageBottomSheet · zh") {
            LanguageBottomSheet(selectedLanguageCode = "zh", onLanguageSelected = {})
        },
        ComponentPreviewCase("LanguageBottomSheet · de") {
            LanguageBottomSheet(selectedLanguageCode = "de", onLanguageSelected = {})
        },
        ComponentPreviewCase("LanguageFlagIcon · Unknown") {
            LanguageFlagIcon("unknown")
        },
        ComponentPreviewCase("PlatformSheetSurface") {
            PlatformSheetSurface {
                Column(Modifier.padding(16.dp)) {
                    SheetDragHandle(
                        sheetState = rememberModalBottomSheetState(),
                        onDismissRequest = {}); Text("Sheet content")
                }
            }
        },
        ComponentPreviewCase("PlatformModalBottomSheet · Expanded") {
            val density = LocalDensity.current
            val sheetState = remember {
                SheetState(
                    skipPartiallyExpanded = true,
                    positionalThreshold = { with(density) { 56.dp.toPx() } },
                    velocityThreshold = { with(density) { 125.dp.toPx() } },
                    initialValue = SheetValue.Expanded
                )
            }
            PlatformModalBottomSheet(
                onDismissRequest = {},
                sheetState = sheetState
            ) { Text("Expanded sheet", Modifier.padding(24.dp)) }
        },
    ),
)

/**
 * Renders named sheet states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 660)
@Preview(name = "Dark", widthDp = 400, heightDp = 660, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 660, fontScale = 1.5f)
@Composable
private fun SheetPreview(@PreviewParameter(SheetPreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
