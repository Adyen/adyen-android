package com.adyen.checkout.core.internal.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/08/2017.
 */
public final class PaymentMethodDeletionResponse extends JsonObject {
    public static final Creator<PaymentMethodDeletionResponse> CREATOR = new DefaultCreator<>(PaymentMethodDeletionResponse.class);

    private static final String KEY_RESULT_CODE = "resultCode";

    private final ResultCode mResultCode;

    public PaymentMethodDeletionResponse(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mResultCode = parseEnum(jsonObject.getString(KEY_RESULT_CODE), ResultCode.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentMethodDeletionResponse that = (PaymentMethodDeletionResponse) o;

        return mResultCode == that.mResultCode;
    }

    @Override
    public int hashCode() {
        return mResultCode != null ? mResultCode.hashCode() : 0;
    }

    @NonNull
    public ResultCode getResultCode() {
        return mResultCode;
    }

    /**
     * The result code.
     */
    public enum ResultCode {
        SUCCESS,
        ERROR
    }
}
