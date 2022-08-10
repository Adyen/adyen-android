/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/8/2022.
 */

package com.adyen.checkout.cse.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.cse.GenericEncrypter
import com.adyen.checkout.cse.exception.EncryptionException
import java.util.Date

/**
 * Test implementation of [GenericEncrypter]. This class should never be used in not test code as it does not do
 * any encryption!
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
class TestGenericEncrypter : GenericEncrypter {

    var shouldThrowException = false

    override fun encryptField(encryptionKey: String, fieldToEncrypt: Any, publicKey: String): String {
        if (shouldThrowException) throw EncryptionException("Failed for testing purposes", null)
        return fieldToEncrypt.toString()
    }

    override fun makeGenerationTime(generationTime: Date?): String {
        return Date().toString()
    }
}
