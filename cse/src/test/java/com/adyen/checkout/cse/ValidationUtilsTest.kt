/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2021.
 */

package com.adyen.checkout.cse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ValidationUtilsTest {

    private static final String PUBLIC_KEYS_JSON = "PublicKeys.json";
    private static final String KEY_VALID_PUBLIC_KEYS = "validPublicKeys";
    private static final String KEY_INVALID_PUBLIC_KEYS = "invalidPublicKeys";

    JSONObject publicKeys;

    @Before
    public void init() throws IOException, JSONException {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        publicKeys = ResourceReader.readJsonFileFromResource(PUBLIC_KEYS_JSON, classLoader);
    }

    private List<String> getPublicKeysArrayFromJson(String key) throws JSONException {
        final List<String> keys = new ArrayList<>();
        final JSONArray validKeysArray = publicKeys.getJSONArray(key);
        for (int i = 0; i < validKeysArray.length(); i++) {
            final String publicKey = validKeysArray.getString(i);
            keys.add(publicKey);
        }
        return keys;
    }

    @Test
    public void isPublicKeyValid_CorrectPattern_ExpectValid() throws JSONException {
        final List<String> validKeys = getPublicKeysArrayFromJson(KEY_VALID_PUBLIC_KEYS);
        for (String validKey : validKeys) {
            Assert.assertTrue(ValidationUtils.isPublicKeyValid(validKey));
        }
    }

    @Test
    public void isPublicKeyValid_IncorrectPattern_ExpectInvalid() throws JSONException {
        final List<String> invalidKeys = getPublicKeysArrayFromJson(KEY_INVALID_PUBLIC_KEYS);
        for (String invalidKey : invalidKeys) {
            Assert.assertFalse(ValidationUtils.isPublicKeyValid(invalidKey));
        }
    }
}
