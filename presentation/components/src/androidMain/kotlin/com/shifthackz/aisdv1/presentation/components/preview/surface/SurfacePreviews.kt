package com.shifthackz.aisdv1.presentation.components.preview.surface

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.platform.PlatformClippedContent
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewCase
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewProvider
import com.shifthackz.aisdv1.presentation.components.preview.ComponentPreviewTheme
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformBlurredPanel
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformLazySurface
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Surface shapes, borders, clickability, joined edges, lazy grouping and all scrollbar overloads.
 *
 * @author Dmitriy Moroz
 */
internal class SurfacePreviewProvider : ComponentPreviewProvider(
    listOf(
        ComponentPreviewCase("PlatformSurface · Plain") {
            PlatformSurface(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Surface content",
                    Modifier.padding(24.dp)
                )
            }
        },
        ComponentPreviewCase("PlatformSurface · Border") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Elevated") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Clickable") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Disabled") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                isEnabled = false,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Clipped") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                shouldClipBackground = true,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Transparent") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                shouldDrawBackground = false,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Joined top") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                isBottomEdgeVisible = false,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Joined middle") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                isTopEdgeVisible = false,
                isBottomEdgeVisible = false,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Joined bottom") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                isTopEdgeVisible = false,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformSurface · Square") {
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
            ) { Text("Surface content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformBlurredPanel") {
            PlatformBlurredPanel { Text("Panel content", Modifier.padding(24.dp)) }
        },
        ComponentPreviewCase("PlatformClippedContent") {
            PlatformClippedContent(Modifier.size(160.dp)) {
                Box(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)
                )
            }
        },
        ComponentPreviewCase("PlatformLazySurface · Complete") {
            val state = rememberLazyListState()
            PlatformLazySurface(state = state, firstItemIndex = 0, lastItemIndex = 3) {
                LazyColumn(
                    state = state,
                    modifier = Modifier.height(280.dp)
                ) { items(4) { SettingsItem(text = "Setting ${it + 1}") } }
            }
        },
        ComponentPreviewCase("PlatformLazySurface · Partial") {
            val state = rememberLazyListState(initialFirstVisibleItemIndex = 2)
            PlatformLazySurface(state = state, firstItemIndex = 0, lastItemIndex = 6) {
                LazyColumn(
                    state = state,
                    modifier = Modifier.height(220.dp)
                ) { items(7) { SettingsItem(text = "Setting ${it + 1}") } }
            }
        },
        ComponentPreviewCase("Scrollbar · ScrollState") {
            val state = rememberScrollState(90)
            Column(Modifier.height(220.dp).verticalScrollbar(state).verticalScroll(state)) {
                repeat(
                    20
                ) { Text("Row $it", Modifier.padding(12.dp)) }
            }
        },
        ComponentPreviewCase("Scrollbar · LazyListState") {
            val state = rememberLazyListState(initialFirstVisibleItemIndex = 2)
            LazyColumn(
                state = state,
                modifier = Modifier.height(220.dp).verticalScrollbar(state)
            ) { items(20) { Text("Row $it", Modifier.padding(12.dp)) } }
        },
        ComponentPreviewCase("Scrollbar · LazyGridState") {
            val state = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.height(220.dp).verticalScrollbar(state)
            ) { items(20) { Text("Cell $it", Modifier.padding(24.dp)) } }
        },
    ),
)

/**
 * Renders named surface states in the app palette without live service bindings.
 *
 * @param case Fixed visual state selected by the tooling provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", widthDp = 400, heightDp = 400)
@Preview(name = "Dark", widthDp = 400, heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact · Large text", widthDp = 320, heightDp = 440, fontScale = 1.5f)
@Composable
private fun SurfacePreview(@PreviewParameter(SurfacePreviewProvider::class) case: ComponentPreviewCase) {
    ComponentPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            case.content()
        }
    }
}
