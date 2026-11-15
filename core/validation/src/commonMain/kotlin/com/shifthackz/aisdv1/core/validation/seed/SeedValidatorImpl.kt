package com.shifthackz.aisdv1.core.validation.seed

import com.shifthackz.aisdv1.core.validation.ValidationResult

/**
 * Checks optional decimal seeds using integer arithmetic without losing precision on either platform.
 *
 * @param maximum inclusive provider limit; must be nonnegative.
 * @author Dmitriy Moroz
 */
internal class SeedValidatorImpl(private val maximum: Long = SeedValidator.MAX_WEB_SEED) :
    SeedValidator {

    init {
        require(maximum >= 0) { "The maximum seed must be nonnegative" }
    }

    /**
     * Validates format before parsing so overflow remains a range error and invalid text is not rewritten.
     *
     * @param input decimal seed, optional empty text, or the `-1` random sentinel.
     * @return validity and a typed format or upper-bound error.
     * @author Dmitriy Moroz
     */
    override fun invoke(input: String?): ValidationResult<SeedValidator.Error> {
        val value = input?.trim().orEmpty()
        if (value.isEmpty() || value == "-1") return ValidationResult(isValid = true)
        if (value.any { it !in '0'..'9' }) {
            return ValidationResult(
                isValid = false,
                validationError = SeedValidator.Error.InvalidFormat
            )
        }
        val seed = value.toLongOrNull()
        return if (seed == null || seed > maximum) {
            ValidationResult(
                isValid = false,
                validationError = SeedValidator.Error.TooLarge(maximum)
            )
        } else {
            ValidationResult(isValid = true)
        }
    }
}
