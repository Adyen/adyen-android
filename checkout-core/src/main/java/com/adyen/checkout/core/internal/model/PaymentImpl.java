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
import com.adyen.checkout.core.model.Payment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public final class PaymentImpl extends JsonObject implements Payment {
    @NonNull
    public static final Parcelable.Creator<PaymentImpl> CREATOR = new DefaultCreator<>(PaymentImpl.class);

    private String mCountryCode;

    private String mReference;

    private Date mSessionValidity;

    private AmountImpl mAmount;

    private String mReturnUrl;

    private String mShopperLocale;

    private String mShopperReference;

    private PaymentImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mCountryCode = jsonObject.getString("countryCode");
        mReference = jsonObject.getString("reference");
        mSessionValidity = parseDate("sessionValidity");
        mAmount = parse("amount", AmountImpl.class);
        mReturnUrl = jsonObject.getString("returnUrl");
        mShopperLocale = jsonObject.getString("shopperLocale");
        mShopperReference = jsonObject.optString("shopperReference", null);
    }

    @NonNull
    @Override
    public AmountImpl getAmount() {
        return mAmount;
    }

    @NonNull
    @Override
    public String getCountryCode() {
        return mCountryCode;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentImpl payment = (PaymentImpl) o;

        if (mCountryCode != null ? !mCountryCode.equals(payment.mCountryCode) : payment.mCountryCode != null) {
            return false;
        }
        if (mReference != null ? !mReference.equals(payment.mReference) : payment.mReference != null) {
            return false;
        }
        if (mSessionValidity != null ? !mSessionValidity.equals(payment.mSessionValidity) : payment.mSessionValidity != null) {
            return false;
        }
        if (mAmount != null ? !mAmount.equals(payment.mAmount) : payment.mAmount != null) {
            return false;
        }
        if (mReturnUrl != null ? !mReturnUrl.equals(payment.mReturnUrl) : payment.mReturnUrl != null) {
            return false;
        }
        if (mShopperLocale != null ? !mShopperLocale.equals(payment.mShopperLocale) : payment.mShopperLocale != null) {
            return false;
        }
        return mShopperReference != null ? mShopperReference.equals(payment.mShopperReference) : payment.mShopperReference == null;
    }

    @Override
    public int hashCode() {
        int result = mCountryCode != null ? mCountryCode.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mReference != null ? mReference.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mSessionValidity != null ? mSessionValidity.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mAmount != null ? mAmount.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mReturnUrl != null ? mReturnUrl.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mShopperLocale != null ? mShopperLocale.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mShopperReference != null ? mShopperReference.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Payment{" + "CountryCode='" + mCountryCode + '\'' + ", Amount=" + mAmount + '}';
    }

    @NonNull
    public String getReference() {
        return mReference;
    }

    @NonNull
    public Date getSessionValidity() {
        return new Date(mSessionValidity.getTime());
    }

    @NonNull
    public String getReturnUrl() {
        return mReturnUrl;
    }

    @NonNull
    public String getShopperLocale() {
        return mShopperLocale;
    }

    @Nullable
    public String getShopperReference() {
        return mShopperReference;
    }
}
