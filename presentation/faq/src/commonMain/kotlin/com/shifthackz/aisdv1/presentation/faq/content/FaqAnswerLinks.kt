package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString

private val faqUrlPattern = Regex(
    """(^|[\s(\[{<"'“‘])(https?://[^\s<>"'“”‘’\\]+)""",
    RegexOption.IGNORE_CASE,
)

/**
 * Adds web links to an answer paragraph while preserving its original text.
 *
 * @param styles Visual treatment of the linked portions of the paragraph.
 * @return The paragraph with absolute HTTP(S) URLs annotated for the platform URI handler.
 * @author Dmitriy Moroz
 */
internal fun String.withFaqLinks(styles: TextLinkStyles): AnnotatedString = buildAnnotatedString {
    append(this@withFaqLinks)
    for (match in faqUrlPattern.findAll(this@withFaqLinks)) {
        val candidate = requireNotNull(match.groups[2])
        val url = candidate.value.trimFaqUrlEnd()
        val authority = url.substringAfter("://").takeWhile { it !in "/?#" }
        if (authority.isNotEmpty() && '@' !in authority) {
            val start = candidate.range.first
            addLink(LinkAnnotation.Url(url, styles), start, start + url.length)
        }
    }
}

private fun String.trimFaqUrlEnd(): String {
    var end = length
    while (end > 0) {
        val last = this[end - 1]
        val opener = when (last) {
            ')' -> '('
            ']' -> '['
            '}' -> '{'
            else -> null
        }
        if (last !in ".,;:!?…" &&
            (opener == null || take(end).count { it == last } <= take(end).count { it == opener })
        ) break
        end--
    }
    return take(end)
}
