/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 28/12/2017.
 */

package com.adyen.checkout.core.internal.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public final class PaymentSessionResponse extends JsonObject {
    @NonNull
    private static final Creator<PaymentSessionResponse> CREATOR = new DefaultCreator<>(PaymentSessionResponse.class);

    private final String mPaymentSession;

    private PaymentSessionResponse(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mPaymentSession = jsonObject.getString("paymentSession");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentSessionResponse that = (PaymentSessionResponse) o;

        return mPaymentSession != null ? mPaymentSession.equals(that.mPaymentSession) : that.mPaymentSession == null;
    }

    @Override
    public int hashCode() {
        return mPaymentSession != null ? mPaymentSession.hashCode() : 0;
    }

    @NonNull
    public String getPaymentSession() {
        return mPaymentSession;
    }
}
