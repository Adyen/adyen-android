/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 13/02/2019.
 */

package com.adyen.checkout.base.model;

import android.content.Context;
import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.adyen.checkout.base.util.FilesUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PaymentMethodsApiResponseTest {

    private static final String PAYMENT_METHODS_RESPONSE_JSON = "paymentMethodsResponse.json";

    @Test
    public void isParsed() throws JSONException {
        JSONObject paymentMethodsResponseJson = getPaymentMethodsResponseJsonObject();

        PaymentMethodsApiResponse paymentMethodsApiResponse = PaymentMethodsApiResponse.SERIALIZER.deserialize(paymentMethodsResponseJson);

        assert paymentMethodsApiResponse.getGroups() != null;
        assert paymentMethodsApiResponse.getGroups().size() == 3;
        assert paymentMethodsApiResponse.getStoredPaymentMethods() == null;
        assert paymentMethodsApiResponse.getPaymentMethods() != null;
        assert paymentMethodsApiResponse.getPaymentMethods().size() == 209;
    }

    @Test
    public void isParceled() throws JSONException {
        JSONObject paymentMethodsResponseJson = getPaymentMethodsResponseJsonObject();

        PaymentMethodsApiResponse paymentMethodsApiResponse = PaymentMethodsApiResponse.SERIALIZER.deserialize(paymentMethodsResponseJson);

        Parcel parcel = Parcel.obtain();
        paymentMethodsApiResponse.writeToParcel(parcel, paymentMethodsApiResponse.describeContents());
        parcel.setDataPosition(0);

        PaymentMethodsApiResponse fromParcel = PaymentMethodsApiResponse.CREATOR.createFromParcel(parcel);

        assert paymentMethodsApiResponse.getGroups().size() == fromParcel.getGroups().size();
        assert paymentMethodsApiResponse.getPaymentMethods().size() == fromParcel.getPaymentMethods().size();
    }

    @NonNull
    private JSONObject getPaymentMethodsResponseJsonObject() throws JSONException {
        Context targetContext = InstrumentationRegistry.getTargetContext();

        String paymentMethodsResponseStr = FilesUtils.read(targetContext, PAYMENT_METHODS_RESPONSE_JSON);
        return new JSONObject(paymentMethodsResponseStr);
    }
}
