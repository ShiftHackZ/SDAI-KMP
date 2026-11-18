package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.shifthackz.aisdv1.presentation.components.generated.resources.Res
import com.shifthackz.aisdv1.presentation.components.generated.resources.sdai_logo
import org.jetbrains.compose.resources.painterResource

/**
 * Returns the shared SDAI logo without exporting generated resource accessors.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun sdaiLogoPainter(): Painter = painterResource(Res.drawable.sdai_logo)
