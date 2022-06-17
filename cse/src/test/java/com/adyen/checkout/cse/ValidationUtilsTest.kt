/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2021.
 */
package com.adyen.checkout.cse

import com.adyen.checkout.cse.ResourceReader.readJsonFileFromResource
import com.adyen.checkout.cse.ValidationUtils.isPublicKeyValid
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ValidationUtilsTest {

    private var publicKeys: JSONObject? = null

    @BeforeEach
    fun init() {
        val classLoader = requireNotNull(this.javaClass.classLoader)
        publicKeys = readJsonFileFromResource(PUBLIC_KEYS_JSON, classLoader)
    }

    @Throws(JSONException::class)
    private fun getPublicKeysArrayFromJson(key: String): List<String> {
        val keys: MutableList<String> = ArrayList()
        val validKeysArray = publicKeys!!.getJSONArray(key)
        for (i in 0 until validKeysArray.length()) {
            val publicKey = validKeysArray.getString(i)
            keys.add(publicKey)
        }
        return keys
    }

    @Test
    fun isPublicKeyValid_CorrectPattern_ExpectValid() {
        val validKeys = getPublicKeysArrayFromJson(KEY_VALID_PUBLIC_KEYS)
        for (validKey in validKeys) {
            assertTrue(isPublicKeyValid(validKey))
        }
    }

    @Test
    fun isPublicKeyValid_IncorrectPattern_ExpectInvalid() {
        val invalidKeys = getPublicKeysArrayFromJson(KEY_INVALID_PUBLIC_KEYS)
        for (invalidKey in invalidKeys) {
            assertFalse(isPublicKeyValid(invalidKey))
        }
    }

    companion object {
        private const val PUBLIC_KEYS_JSON = "PublicKeys.json"
        private const val KEY_VALID_PUBLIC_KEYS = "validPublicKeys"
        private const val KEY_INVALID_PUBLIC_KEYS = "invalidPublicKeys"
    }
}
