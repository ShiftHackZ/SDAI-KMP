package com.shifthackz.aisdv1.network.client

/**
 * Controlled size rejection without server payload or internal text exposed to UI.
 * A null message selects the application's existing localized generic error.
 *
 * @author Dmitriy Moroz
 */
class ResponseSizeLimitException : IllegalStateException()
