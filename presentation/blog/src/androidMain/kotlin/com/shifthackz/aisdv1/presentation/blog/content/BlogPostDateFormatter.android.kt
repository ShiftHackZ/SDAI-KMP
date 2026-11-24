package com.shifthackz.aisdv1.presentation.blog.content

import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal actual fun formatBlogPostDate(isoDate: String, languageCode: String): String {
    val utc = TimeZone.getTimeZone("UTC")
    val position = ParsePosition(0)
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = utc
        isLenient = false
    }.parse(isoDate, position) ?: return isoDate
    if (position.index != isoDate.length) return isoDate

    val deviceLocale = Locale.getDefault(Locale.Category.FORMAT)
    val displayLocale =
        deviceLocale.takeIf { it.language == languageCode } ?: Locale.forLanguageTag(languageCode)
    return DateFormat.getDateInstance(DateFormat.MEDIUM, displayLocale).apply {
        timeZone = utc
    }.format(date)
}
