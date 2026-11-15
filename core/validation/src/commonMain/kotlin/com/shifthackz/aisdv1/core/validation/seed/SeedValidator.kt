package com.shifthackz.aisdv1.core.validation.seed

import com.shifthackz.aisdv1.core.validation.ValidationResult

/**
 * Validates optional decimal generation seeds without truncating or rounding their text.
 * Empty input and `-1` request server-selected randomness. Fixed seeds must contain ASCII
 * digits and fit in the configured range. The default range is exactly representable by
 * JavaScript numeric controls; native providers can supply their own upper bound.
 *
 * @author Dmitriy Moroz
 */
interface SeedValidator {

    /**
     * Checks a seed string, accepting surrounding whitespace as the payload mapper does.
     *
     * @param input decimal seed, `-1`, or an absent/empty value for server-selected randomness.
     * @return a validation result retaining the reason for invalid input.
     * @author Dmitriy Moroz
     */
    operator fun invoke(input: String?): ValidationResult<Error>

    /**
     * Reasons a decimal seed cannot be submitted without changing its meaning.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Error {
        /**
         * Input is neither an optional value, the random sentinel, nor unsigned decimal digits.
         *
         * @author Dmitriy Moroz
         */
        data object InvalidFormat : Error

        /**
         * Input exceeds the provider's supported numeric range, including integer overflow.
         *
         * @param maximum inclusive upper bound that the user can enter.
         * @author Dmitriy Moroz
         */
        data class TooLarge(val maximum: Long) : Error
    }

    companion object {
        /**
         * Largest safe integer for JavaScript numeric web controls.
         *
         * @author Dmitriy Moroz
         */
        const val MAX_WEB_SEED = 9_007_199_254_740_991L
    }
}
