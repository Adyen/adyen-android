/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/07/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.model.Amount;

import org.json.JSONException;
import org.json.JSONObject;

public final class AmountImpl extends JsonObject implements Amount {
    @NonNull
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
    public boolean equals(@Nullable Object o) {
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
        int result = Long.valueOf(mValue).hashCode();
        result = HashUtils.MULTIPLIER * result + (mCurrency != null ? mCurrency.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Amount{" + "Value=" + mValue + ", Currency='" + mCurrency + '\'' + '}';
    }
}
