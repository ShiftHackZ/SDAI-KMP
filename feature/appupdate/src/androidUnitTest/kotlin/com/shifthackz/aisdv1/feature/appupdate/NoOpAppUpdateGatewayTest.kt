package com.shifthackz.aisdv1.feature.appupdate

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Ensures unsupported distributions remain inert across repeated discovery requests.
 *
 * @author Dmitriy Moroz
 */
class NoOpAppUpdateGatewayTest {
    @Test
    fun `given FOSS discovery, repeated checks are requested, expected no offers or external operations`() =
        runTest {
            // Given
            val gateway = NoOpAppUpdateGateway

            // When
            val first = gateway.check()
            val second = gateway.check()

            // Then
            assertNull(first)
            assertNull(second)
        }

}
