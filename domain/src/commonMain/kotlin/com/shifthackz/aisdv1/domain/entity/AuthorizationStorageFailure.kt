package com.shifthackz.aisdv1.domain.entity

/**
 * Safe storage failure without secret-bearing platform messages or cause chains.
 * Absence is represented by anonymous credentials; these failures never imply absence.
 *
 * @param reason Category used for localized recovery guidance.
 * @author Dmitriy Moroz
 */
class AuthorizationStorageFailure(val reason: Reason) :
    Exception("Authorization storage ${reason.name}") {
    /**
     * Storage availability and integrity categories, without platform exception details.
     *
     * @author Dmitriy Moroz
     */
    enum class Reason { UNAVAILABLE, UNREADABLE, PERSISTENCE }
}
