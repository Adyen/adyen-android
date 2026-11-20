/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/9/2023.
 */

package com.adyen.checkout.cse.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object GenericEncryptorFactory {

    fun provide(): BaseGenericEncryptor {
        val dateGenerator = DateGenerator()
        val jweEncryptor = JSONWebEncryptor()
        return DefaultGenericEncryptor(dateGenerator, jweEncryptor)
    }
}
