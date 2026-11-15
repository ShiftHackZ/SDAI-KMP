package com.shifthackz.aisdv1.feature.auth.crypto

import com.shifthackz.aisdv1.feature.auth.AuthorizationKeyValueStoreProvider

/**
 * Android protected-store provider; construction performs no crypto or Binder operations.
 *
 * @author Dmitriy Moroz
 */
internal interface CryptoProvider : AuthorizationKeyValueStoreProvider
