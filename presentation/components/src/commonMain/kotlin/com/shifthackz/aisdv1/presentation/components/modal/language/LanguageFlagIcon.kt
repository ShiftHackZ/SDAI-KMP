package com.shifthackz.aisdv1.presentation.components.modal.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.generated.resources.Res
import com.shifthackz.aisdv1.presentation.components.generated.resources.flag_cn
import com.shifthackz.aisdv1.presentation.components.generated.resources.flag_de
import com.shifthackz.aisdv1.presentation.components.generated.resources.flag_gb
import com.shifthackz.aisdv1.presentation.components.generated.resources.flag_ru
import com.shifthackz.aisdv1.presentation.components.generated.resources.flag_tr
import com.shifthackz.aisdv1.presentation.components.generated.resources.flag_ua
import org.jetbrains.compose.resources.painterResource

/**
 * Renders the same bundled flag at a fixed size on Android and iOS language rows.
 * Unknown language codes omit the icon; the adjacent native language name labels each row.
 *
 * @param languageCode Supported language code identifying the bundled flag.
 * @param modifier Size and placement modifiers applied before the default flag spacing.
 * @author Dmitriy Moroz
 */
@Composable
internal fun LanguageFlagIcon(languageCode: String, modifier: Modifier = Modifier) {
    val flag = when (languageCode) {
        "en" -> Res.drawable.flag_gb
        "uk" -> Res.drawable.flag_ua
        "tr" -> Res.drawable.flag_tr
        "ru" -> Res.drawable.flag_ru
        "zh" -> Res.drawable.flag_cn
        "de" -> Res.drawable.flag_de
        else -> return
    }
    Image(
        modifier = modifier.padding(horizontal = 8.dp).size(width = 32.dp, height = 24.dp),
        painter = painterResource(flag),
        contentDescription = null,
    )
}
