package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Gives primary, secondary and dialog actions the same 48dp minimum visible height.
 * Labels may grow with text scaling or wrapping; caller-owned outer spacing stays outside the button.
 * Explicit geometry for illustration controls and image tiles remains caller-owned.
 *
 * @return This modifier followed by the shared action height constraint.
 * @author Dmitriy Moroz
 */
fun Modifier.actionButtonSize(): Modifier = heightIn(min = 48.dp)
