package com.shifthackz.aisdv1.data.image

import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupImageValidatorImplTest {
    private val decoder = mockk<BackupImageDecoder>(relaxed = true)
    private val budget = BackupImageBudget(1024, 16_777_216, 128L * 1024 * 1024, 256)
    private val validator = BackupImageValidatorImpl(decoder, budget)
    private val encoded = byteArrayOf(1)

    @Test
    fun `given compact image with enormous dimensions, native probe reports dimensions, expected pixel rejection before content allocation`() {
        // Given
        every { decoder.dimensions(encoded) } returns (16_384L to 16_384L)
        // When
        val failure = assertThrows(IllegalArgumentException::class.java) { validator(encoded) }
        // Then
        assertEquals("Backup image exceeds pixel budget", failure.message)
        verify(exactly = 0) { decoder.validateSample(any(), any()) }
    }

    @Test
    fun `given image metadata beyond integral multiplication range, native probe reports dimensions, expected rejection without overflow or sample allocation`() {
        // Given
        every { decoder.dimensions(encoded) } returns (Long.MAX_VALUE to Long.MAX_VALUE)
        // When
        assertThrows(IllegalArgumentException::class.java) { validator(encoded) }
        // Then
        verify(exactly = 0) { decoder.validateSample(any(), any()) }
    }

    @Test
    fun `given pixel count admitted but decoded bytes over budget, validation begins, expected byte admission before sample allocation`() {
        // Given
        val constrained = BackupImageValidatorImpl(decoder, budget.copy(maxDecodedBytes = 7))
        every { decoder.dimensions(encoded) } returns (1L to 1L)
        // When
        val failure = assertThrows(IllegalArgumentException::class.java) { constrained(encoded) }
        // Then
        assertEquals("Backup image exceeds decoded byte budget", failure.message)
        verify(exactly = 0) { decoder.validateSample(any(), any()) }
    }

    @Test
    fun `given encoded image exceeds admission, validation starts, expected native decoder never called`() {
        // Given
        val oversized = ByteArray(1025)
        // When
        assertThrows(IllegalArgumentException::class.java) { validator(oversized) }
        // Then
        verify(exactly = 0) { decoder.dimensions(any()); decoder.validateSample(any(), any()) }
    }

    @Test
    fun `given admitted dimensions, image validation runs, expected bounded native sample without original bitmap`() {
        // Given
        every { decoder.dimensions(encoded) } returns (4096L to 4096L)
        // When
        validator(encoded)
        // Then
        verify(exactly = 1) { decoder.validateSample(encoded, 256) }
    }
}
