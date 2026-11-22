package com.shifthackz.aisdv1.domain.entity

/**
 * Short-lived authorization proof
 * never logged or persisted in gallery metadata.
 *
 * @param code One-time server authorization code.
 * @param verifier Original PKCE secret retained by the native authorization session.
 * @author Dmitriy Moroz
 */
class CivitaiOAuthCode(val code: String, val verifier: String)
