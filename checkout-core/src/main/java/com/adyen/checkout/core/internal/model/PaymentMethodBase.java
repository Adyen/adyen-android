package com.adyen.checkout.core.internal.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 01/05/2018.
 */
public class PaymentMethodBase extends JsonObject {
    public static final Creator<PaymentMethodBase> CREATOR = new DefaultCreator<>(PaymentMethodBase.class);

    private final String mType;

    protected PaymentMethodBase(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mType = jsonObject.getString("type");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentMethodBase that = (PaymentMethodBase) o;

        return mType != null ? mType.equals(that.mType) : that.mType == null;
    }

    @Override
    public int hashCode() {
        return mType != null ? mType.hashCode() : 0;
    }

    @NonNull
    public String getType() {
        return mType;
    }
}
