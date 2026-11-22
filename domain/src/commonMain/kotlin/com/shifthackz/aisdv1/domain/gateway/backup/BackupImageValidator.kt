package com.shifthackz.aisdv1.domain.gateway.backup

/**
 * Checks native image dimensions before allocation and validates content through a bounded platform sample.
 *
 * @author Dmitriy Moroz
 */
interface BackupImageValidator {
    /**
     * Rejects over-budget dimensions, corrupt or unsupported images without decoding an original-size bitmap.
     *
     * @param bytes Complete bytes of one bounded gallery image.
     * @author Dmitriy Moroz
     */
    operator fun invoke(bytes: ByteArray)
}
