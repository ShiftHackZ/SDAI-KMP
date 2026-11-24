package com.shifthackz.aisdv1.presentation.blog.content

import java.util.Locale
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks the native formatter's locale choices and stable date-only output.
 *
 * @author Dmitriy Moroz
 */
class BlogPostDateFormatterTest {
    private val previousLocale = Locale.getDefault(Locale.Category.FORMAT)
    private val previousZone = TimeZone.getDefault()

    @After
    fun restoreDefaults() {
        Locale.setDefault(Locale.Category.FORMAT, previousLocale)
        TimeZone.setDefault(previousZone)
    }

    @Test
    fun `given blog date formatter and British English device, an English publication date is formatted, expected day first without timezone shift`() {
        // Given
        Locale.setDefault(Locale.Category.FORMAT, Locale.UK)
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))

        // When
        val result = formatBlogPostDate("2026-11-24", "en")

        // Then
        assertEquals("24 Nov 2026", result)
    }

    @Test
    fun `given blog date formatter and German app language, publication date is formatted on a US device, expected German date`() {
        // Given
        Locale.setDefault(Locale.Category.FORMAT, Locale.US)

        // When
        val result = formatBlogPostDate("2026-11-24", "de")

        // Then
        assertEquals("24.11.2026", result)
    }

    @Test
    fun `given blog date formatter and invalid calendar day, publication date is formatted, expected original publisher text`() {
        // Given
        val publishedOn = "2026-02-31"

        // When
        val result = formatBlogPostDate(publishedOn, "en")

        // Then
        assertEquals(publishedOn, result)
    }
}
