/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/8/2022.
 */

package com.adyen.checkout.cse.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.cse.BaseGenericEncrypter
import com.adyen.checkout.cse.exception.EncryptionException

/**
 * Test implementation of [BaseGenericEncrypter]. This class should never be used in not test code as it does not do
 * any encryption!
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
class TestGenericEncrypter : BaseGenericEncrypter {

    var shouldThrowException = false

    override fun encryptField(fieldKeyToEncrypt: String, fieldValueToEncrypt: Any?, publicKey: String): String {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)
        return fieldValueToEncrypt.toString()
    }

    override fun encryptFields(publicKey: String, vararg fieldsToEncrypt: Pair<String, Any?>): String {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)
        return fieldsToEncrypt.firstOrNull().toString()
    }
}
