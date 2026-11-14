package com.shifthackz.aisdv1.core.validation.di

import com.shifthackz.aisdv1.core.validation.common.CommonStringValidator
import com.shifthackz.aisdv1.core.validation.common.CommonStringValidatorImpl
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidatorImpl
import com.shifthackz.aisdv1.core.validation.path.FilePathValidator
import com.shifthackz.aisdv1.core.validation.path.FilePathValidatorImpl
import com.shifthackz.aisdv1.core.validation.seed.SeedValidator
import com.shifthackz.aisdv1.core.validation.seed.SeedValidatorImpl
import com.shifthackz.aisdv1.core.validation.url.UrlValidator
import com.shifthackz.aisdv1.core.validation.url.UrlValidatorImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Identifies the seed validator configured for providers that accept the full signed Long range.
 *
 * @author Dmitriy Moroz
 */
val nativeSeedValidatorQualifier = named("native_seed_validator")

/**
 * Exposes the `validatorsModule` value used by the SDAI validation layer.
 *
 * @author Dmitriy Moroz
 */
val validatorsModule = module {
    // !!! Do not use [factoryOf] for DimensionValidatorImpl, it has 2 default Ints in constructor
    factory<DimensionValidator> { DimensionValidatorImpl() }
    factory<UrlValidator> { UrlValidatorImpl() }

    factoryOf(::CommonStringValidatorImpl) bind CommonStringValidator::class
    factoryOf(::FilePathValidatorImpl) bind FilePathValidator::class
    factory<SeedValidator> { SeedValidatorImpl() }
    factory<SeedValidator>(nativeSeedValidatorQualifier) { SeedValidatorImpl(Long.MAX_VALUE) }
}
