package com.shifthackz.aisdv1.domain.validator

import com.shifthackz.aisdv1.domain.entity.mnn.MnnBundleManifest

/**
 * Verifies complete, immutable SD 1.5 payloads; invalid data always fails explicitly.
 *
 * @author Dmitriy Moroz
 */
interface MnnBundleValidator {
    /**
     * Checks supported metadata, file ownership, exact lengths and SHA256s before use.
     *
     * @param path Owned staging or selected bundle root.
     * @return Validated identity; throws when the bundle cannot be used.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(path: String): MnnBundleManifest
}
