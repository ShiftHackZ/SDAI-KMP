package com.shifthackz.aisdv1.domain.entity

/**
 * Durable publication outcome. UNCERTAIN forbids automatic reposting after an ambiguous server response.
 *
 * @author Dmitriy Moroz
 */
enum class CivitaiUploadStatus { UPLOADING, PUBLISHED, FAILED, UNCERTAIN }
