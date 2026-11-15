package com.shifthackz.aisdv1.core.validation.seed

import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.dsl.koinApplication

/**
 * Covers optional randomness, exact web integer boundaries, overflow and invalid decimal text.
 *
 * @author Dmitriy Moroz
 */
class SeedValidatorTest {
    private val dependencyGraph = koinApplication { modules(validatorsModule) }
    private val validator = dependencyGraph.koin.get<SeedValidator>()

    @After
    fun closeDependencies() {
        dependencyGraph.close()
    }

    @Test
    fun `empty absent and random sentinel values are accepted`() {
        listOf(null, "", " ", "-1", " -1 ").forEach { input ->
            assertTrue("Expected optional seed $input", validator(input).isValid)
        }
    }

    @Test
    fun `zero A1111 random boundary and largest web safe integer are accepted`() {
        listOf("0", "0000", "4294967293", "9007199254740991", " 123 ").forEach { input ->
            assertTrue("Expected exact seed $input", validator(input).isValid)
        }
    }

    @Test
    fun `unsafe web integers and overflowing digit strings report the allowed maximum`() {
        listOf("9007199254740992", "6687881544318117707", Long.MAX_VALUE.toString(), "9".repeat(100))
            .forEach { input ->
                assertEquals(SeedValidator.Error.TooLarge(9_007_199_254_740_991L), validator(input).validationError)
            }
    }

    @Test
    fun `negative fractional signed exponential and non ASCII numbers are rejected`() {
        listOf("-2", "-", "1.5", "+1", "1e3", "12x", "١٢", "1 2").forEach { input ->
            assertEquals(SeedValidator.Error.InvalidFormat, validator(input).validationError)
        }
    }

    @Test
    fun `native range permits exact Long values without applying the web limit`() {
        val nativeValidator = dependencyGraph.koin.get<SeedValidator>(nativeSeedValidatorQualifier)
        assertTrue(nativeValidator(Long.MAX_VALUE.toString()).isValid)
        assertEquals(
            SeedValidator.Error.TooLarge(Long.MAX_VALUE),
            nativeValidator("9223372036854775808").validationError,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a negative maximum is rejected`() {
        SeedValidatorImpl(-1)
    }
}
