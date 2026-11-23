package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.displayString

/**
 * Places the platform brand mark, platform icon and build version above drawer destinations.
 *
 * @param buildInfoProvider Build label shown beneath the SDAI name.
 * @param modifier Header placement within the drawer.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DrawerHeader(
    buildInfoProvider: BuildInfoProvider,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 28.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DrawerBrandLogo(modifier = Modifier.size(56.dp))
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "SDAI",
                    style = MaterialTheme.typography.headlineMedium,
                )
                DrawerPlatformIcon(modifier = Modifier.size(18.dp))
            }
            Text(
                text = buildInfoProvider.displayString(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
