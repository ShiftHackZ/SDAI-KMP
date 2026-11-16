package com.shifthackz.aisdv1.domain.mocks

import org.junit.Assert.assertTrue

/**
 * Checks that coroutine stack-trace recovery or typed translation retains the original failure.
 * The bounded traversal avoids hanging a failing test on a malformed cyclic cause chain.
 * @param expected Original failure emitted by the isolated collaborator.
 * @param actual Failure returned or thrown by the production operation.
 * @author Dmitriy Moroz
 */
internal fun assertRetainsFailure(expected: Throwable, actual: Throwable?) {
    assertTrue(
        "Original failure must remain in the cause chain",
        generateSequence(actual) { it.cause }.take(16).any { it === expected })
}
