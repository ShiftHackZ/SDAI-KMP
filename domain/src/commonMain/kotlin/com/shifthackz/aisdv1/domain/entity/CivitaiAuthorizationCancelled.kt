package com.shifthackz.aisdv1.domain.entity

/**
 * Indicates that the user closed or declined Civitai authorization without creating a session.
 * Callers may return to the signed-out screen without showing a transport or publication error.
 *
 * @author Dmitriy Moroz
 */
class CivitaiAuthorizationCancelled : Exception("CIVITAI_AUTH_CANCELLED")
