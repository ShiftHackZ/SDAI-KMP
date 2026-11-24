package com.shifthackz.aisdv1.presentation.blog.content

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.timeZoneForSecondsFromGMT

internal actual fun formatBlogPostDate(isoDate: String, languageCode: String): String {
    val utc = NSTimeZone.timeZoneForSecondsFromGMT(0)
    val date = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = "en_US_POSIX")
        timeZone = utc
        dateFormat = "yyyy-MM-dd"
    }.dateFromString(isoDate) ?: return isoDate

    val deviceLocale = NSLocale.currentLocale
    val displayLocale = deviceLocale.takeIf { it.languageCode == languageCode } ?: NSLocale(
        localeIdentifier = languageCode
    )
    return NSDateFormatter().apply {
        locale = displayLocale
        timeZone = utc
        dateStyle = NSDateFormatterMediumStyle
    }.stringFromDate(date)
}
