package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private const val CHAR_BOLD_START = '['

private const val CHAR_BOLD_END = ']'

/**
 * Converts bracket-delimited emphasis in localized onboarding copy to weighted text spans.
 * Square brackets switch between bold and light weight and are omitted from the rendered text.
 *
 * @param value Localized text containing optional bracket-delimited emphasized passages.
 *
 * @return Annotated text preserving all characters except emphasis delimiters.
 *
 * @author Dmitriy Moroz
 */
fun buildOnBoardingText(value: String): AnnotatedString = buildAnnotatedString {
    val parts = mutableListOf<Pair<String, FontWeight>>()
    var currentSequence = ""
    var currentFontWeight = FontWeight.Light

    fun addPart() {
        if (currentSequence.isNotEmpty()) {
            parts.add(currentSequence to currentFontWeight)
            currentSequence = ""
        }
    }

    value.forEach { char ->
        when (char) {
            CHAR_BOLD_START -> {
                addPart()
                currentFontWeight = FontWeight.Bold
            }

            CHAR_BOLD_END -> {
                addPart()
                currentFontWeight = FontWeight.Light
            }

            else -> currentSequence += char
        }
    }
    addPart()

    parts.forEach { (text, weight) ->
        withStyle(style = SpanStyle(fontWeight = weight)) {
            append(text)
        }
    }
}
