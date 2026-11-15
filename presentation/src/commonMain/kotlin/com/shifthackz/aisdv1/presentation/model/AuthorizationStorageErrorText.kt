package com.shifthackz.aisdv1.presentation.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure

/**
 * Maps safe storage categories to localized guidance without exposing platform failure details.
 *
 * @author Dmitriy Moroz
 */
internal fun AuthorizationStorageFailure.localizedDescription(): String = when (reason) {
    AuthorizationStorageFailure.Reason.UNAVAILABLE -> Localization.string("auth_storage_unavailable")
    AuthorizationStorageFailure.Reason.UNREADABLE -> Localization.string("auth_storage_unreadable")
    AuthorizationStorageFailure.Reason.PERSISTENCE -> Localization.string("auth_storage_write_failed")
}
