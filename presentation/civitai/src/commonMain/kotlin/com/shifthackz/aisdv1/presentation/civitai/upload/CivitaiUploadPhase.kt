package com.shifthackz.aisdv1.presentation.civitai.upload

/**
 * Single upload interaction phase, independent from neighboring gallery pages.
 *
 * @author Dmitriy Moroz
 */
enum class CivitaiUploadPhase { IDLE, COMMUNICATING, LOGIN_REQUIRED, SUCCESS, FAILURE }
