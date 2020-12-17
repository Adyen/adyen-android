/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */

package com.adyen.checkout.components;

import com.adyen.checkout.components.model.PaymentMethodsApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataProvider {
    public static JSONObject readJsonFileFromResource(String fileName, ClassLoader classLoader) throws IOException, JSONException {
        File file = new File(classLoader.getResource(fileName).getFile());
        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return new JSONObject(new String(encoded, Charset.defaultCharset()));
    }

    public static PaymentMethodsApiResponse getPaymentMethodResponse(ClassLoader classLoader) throws IOException, JSONException {
        return PaymentMethodsApiResponse.SERIALIZER.deserialize(readJsonFileFromResource("PaymentMethodsResponse.json", classLoader));
    }
}
