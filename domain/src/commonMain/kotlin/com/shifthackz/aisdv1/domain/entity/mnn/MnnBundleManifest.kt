package com.shifthackz.aisdv1.domain.entity.mnn

/**
 * Validated bundle identity and payload inventory used before model activation and loading.
 *
 * @param profile Exact runtime graph and generation profile.
 * @param files Complete payload inventory; the manifest never hashes itself.
 * @param activationId SHA256 of the exact validated manifest bytes.
 *
 * @author Dmitriy Moroz
 */
data class MnnBundleManifest(
    val profile: String,
    val files: List<MnnBundleFile>,
    val activationId: String
)
