package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.layout.LocalPinnableContainer

/**
 * Keeps a native first-responder editor's lazy item alive while it is focused. UIKit focus does
 * not participate in Compose's focus pinning, so the surrounding item needs an explicit handle.
 * Blur, a container change or removal from composition releases that handle exactly once.
 * Editors outside a pinnable container require no retention.
 *
 * @param isFocused Whether the native UITextField or UITextView is the current editing responder.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun NativeEditorFocusRetention(isFocused: Boolean) {
    val pinnableContainer = LocalPinnableContainer.current
    DisposableEffect(isFocused, pinnableContainer) {
        val pinnedHandle = if (isFocused) pinnableContainer?.pin() else null
        onDispose { pinnedHandle?.release() }
    }
}
