package com.shifthackz.aisdv1.presentation.civitai.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsToggleItem
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformOutlinedTextField
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsList
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsListDefaults
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialog

/**
 * Uses the same Settings rows, typography and modal feedback as the rest of SDAI.
 *
 * @param state Credential-free account and history view.
 * @param processIntent Account and consent action sink.
 * @param openUrl Native browser destination handler.
 * @param modifier Layout of the complete standalone route.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CivitaiContent(
    state: CivitaiState,
    processIntent: (CivitaiIntent) -> Unit,
    openUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var key by remember { mutableStateOf("") }
    var shouldShowApiKey by remember { mutableStateOf(false) }
    val headerModifier = Modifier.padding(SettingsListDefaults.headerPadding)
    val itemModifier = Modifier.fillMaxWidth().padding(SettingsListDefaults.itemPadding)
    val supportingTextModifier =
        Modifier.fillMaxWidth().padding(SettingsListDefaults.supportingTextPadding)
    PlatformScaffold(
        modifier = modifier.fillMaxSize(),
        canUnderlapToolbar = true,
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(
                        Localization.string("civitai_title"),
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                    )
                },
                navigationIcon = { ToolbarBackButton(onClick = { processIntent(CivitaiIntent.Back) }) },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
    ) { padding ->
        SettingsList(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item {
                SettingsHeader(
                    text = Localization.string("civitai_account").asUiText(),
                    modifier = headerModifier,
                )
            }
            if (state.account == null) {
                item {
                    SettingsItem(
                        modifier = itemModifier,
                        startIcon = Icons.AutoMirrored.Filled.Login,
                        text = Localization.string("civitai_sign_in").asUiText(),
                        onClick = { processIntent(CivitaiIntent.SignIn) },
                    )
                }
                item {
                    SettingsItem(
                        modifier = itemModifier,
                        startIcon = Icons.Default.Key,
                        text = Localization.string("civitai_connect").asUiText(),
                        onClick = { shouldShowApiKey = !shouldShowApiKey },
                    )
                }
                if (shouldShowApiKey) {
                    item {
                        Text(
                            Localization.string("civitai_key_help"),
                            modifier = supportingTextModifier,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    item {
                        PlatformOutlinedTextField(
                            value = key,
                            onValueChange = { key = it },
                            modifier = itemModifier,
                            label = Localization.string("civitai_api_key"),
                            containerColor = MaterialTheme.colorScheme.background,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            hintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            trailingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                        )
                    }
                    item {
                        PrimaryButton(
                            onClick = {
                                val submitted = key
                                key = ""
                                processIntent(
                                    CivitaiIntent.Connect(
                                        submitted
                                    )
                                )
                            },
                            isEnabled = key.isNotBlank() && !state.isBusy,
                            modifier = itemModifier,
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(Localization.string("civitai_connect"))
                        }
                    }
                    item {
                        SettingsItem(
                            modifier = itemModifier,
                            startIcon = Icons.Default.ManageAccounts,
                            text = Localization.string("civitai_get_key").asUiText(),
                            onClick = { openUrl("https://civitai.com/user/account") })
                    }
                }
            } else {
                item {
                    SettingsItem(
                        modifier = itemModifier,
                        startIcon = Icons.Default.AccountCircle,
                        text = Localization.string("civitai_account").asUiText(),
                        endValueText = state.account.username.asUiText(),
                        onClick = { openUrl("https://civitai.com/user/account") },
                    )
                }
                item {
                    SettingsItem(
                        modifier = itemModifier,
                        startIcon = Icons.AutoMirrored.Filled.Logout,
                        text = Localization.string("civitai_disconnect").asUiText(),
                        onClick = { processIntent(CivitaiIntent.Disconnect) })
                }
            }
            item {
                SettingsHeader(
                    text = Localization.string("title_settings").asUiText(),
                    modifier = headerModifier,
                )
            }
            item {
                SettingsItem(
                    text = Localization.string("civitai_history").asUiText(),
                    modifier = itemModifier,
                    startIcon = Icons.Default.History,
                    onClick = { processIntent(CivitaiIntent.OpenHistory) },
                )
            }
            item {
                SettingsToggleItem(
                    modifier = itemModifier,
                    startIcon = Icons.Default.CloudUpload,
                    text = Localization.string("civitai_auto_upload").asUiText(),
                    isChecked = state.isAutoUploadEnabled,
                    isEnabled = state.account != null && !state.isBusy,
                    onCheckedChange = { processIntent(if (it) CivitaiIntent.EnableAutoUpload else CivitaiIntent.DisableAutoUpload) },
                )
            }
            item {
                Text(
                    Localization.string("civitai_public_notice"),
                    modifier = supportingTextModifier,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
    if (state.isBusy) ProgressDialog(canDismiss = false)
    if (state.hasFailed) ErrorDialog(
        text = Localization.string("error_generic").asUiText(),
        onDismissRequest = { processIntent(CivitaiIntent.Dismiss) })
    if (state.shouldConfirmAutoUpload) DecisionInteractiveDialog(
        title = Localization.string("civitai_auto_upload").asUiText(),
        text = Localization.string("civitai_public_notice").asUiText(),
        confirmActionText = Localization.string("civitai_enable").asUiText(),
        dismissActionText = Localization.string("cancel").asUiText(),
        onConfirmAction = { processIntent(CivitaiIntent.ConfirmAutoUpload) },
        onDismissRequest = { processIntent(CivitaiIntent.Dismiss) },
    )
}
