package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.Amount;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/07/2018.
 */
public final class AmountImpl extends JsonObject implements Amount {
    public static final Parcelable.Creator<AmountImpl> CREATOR = new DefaultCreator<>(AmountImpl.class);

    private final long mValue;

    private final String mCurrency;

    private AmountImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mValue = jsonObject.getLong("value");
        mCurrency = jsonObject.getString("currency");
    }

    @Override
    public long getValue() {
        return mValue;
    }

    @NonNull
    @Override
    public String getCurrency() {
        return mCurrency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AmountImpl amount = (AmountImpl) o;

        if (mValue != amount.mValue) {
            return false;
        }
        return mCurrency != null ? mCurrency.equals(amount.mCurrency) : amount.mCurrency == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (mValue ^ (mValue >>> 32));
        result = 31 * result + (mCurrency != null ? mCurrency.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Amount{" + "Value=" + mValue + ", Currency='" + mCurrency + '\'' + '}';
    }
}
