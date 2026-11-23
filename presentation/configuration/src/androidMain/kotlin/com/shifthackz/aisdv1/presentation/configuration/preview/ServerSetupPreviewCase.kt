package com.shifthackz.aisdv1.presentation.configuration.preview

import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Names a fixed wizard snapshot and the initial position within its configuration form.
 *
 * @param name Label identifying the provider, validation or operation state.
 * @param state Fixed wizard data rendered without ViewModel or account services.
 * @param formOffset Initial vertical offset for lower controls inside the form's first list item.
 * @author Dmitriy Moroz
 */
internal data class ServerSetupPreviewCase(
    val name: String,
    val state: ServerSetupState,
    val formOffset: Int = 0,
)
