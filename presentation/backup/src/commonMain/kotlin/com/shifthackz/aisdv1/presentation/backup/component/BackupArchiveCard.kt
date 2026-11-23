package com.shifthackz.aisdv1.presentation.backup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.presentation.backup.platform.formatBackupCreatedAt
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Groups the selected archive identity, available source location and creation details before restore choices.
 *
 * @param archive Validated summary with no provider credentials or decoded image data.
 * @param modifier Card placement within the scrolling restore content.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun BackupArchiveCard(archive: BackupInspection, modifier: Modifier = Modifier) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        shape = CardDefaults.outlinedShape,
        color = CardDefaults.outlinedCardColors().containerColor,
        contentColor = CardDefaults.outlinedCardColors().contentColor,
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(archive.filename, style = MaterialTheme.typography.titleMedium)
                if (archive.sourceLocation.isNotEmpty()) {
                    Text(
                        archive.sourceLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    Localization.string(
                        "backup_archive_details",
                        archive.appVersion,
                        archive.sourcePlatform
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    Localization.string(
                        "backup_created_at",
                        formatBackupCreatedAt(archive.createdAt)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
