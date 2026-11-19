package com.shifthackz.aisdv1.data.image

import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import com.shifthackz.aisdv1.domain.gateway.backup.BackupImageValidator

/**
 * Admits native header dimensions before any content bitmap is allocated, then validates a small sample.
 * @param decoder Backup-specific metadata probe and bounded platform content decoder.
 * @param budget Shared encoded, original-pixel and sampled-allocation admission limits.
 * @author Dmitriy Moroz
 */
internal class BackupImageValidatorImpl(
    private val decoder: BackupImageDecoder,
    private val budget: BackupImageBudget,
) : BackupImageValidator {
    override fun invoke(bytes: ByteArray) {
        require(bytes.isNotEmpty() && bytes.size <= budget.maxEncodedBytes) { "Backup image exceeds encoded budget" }
        val (width, height) = decoder.dimensions(bytes)
        require(width > 0 && height > 0 && width <= budget.maxPixels / height) { "Backup image exceeds pixel budget" }
        val pixels = width * height
        // PNG may carry 16-bit RGBA; use eight bytes per pixel without overflowing a multiplication.
        require(pixels <= budget.maxDecodedBytes / 8) { "Backup image exceeds decoded byte budget" }
        decoder.validateSample(bytes, budget.validationEdge)
    }
}
