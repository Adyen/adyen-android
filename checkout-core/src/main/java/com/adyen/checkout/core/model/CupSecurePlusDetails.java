/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 01/05/2018.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.internal.model.AdditionalPaymentMethodDetails;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.internal.model.PaymentInitiationResponse;
import com.adyen.checkout.core.internal.model.ThreeDSecureRedirectData;

import org.json.JSONException;
import org.json.JSONObject;

public final class CupSecurePlusDetails extends AdditionalPaymentMethodDetails {
    @NonNull
    public static final Creator<CupSecurePlusDetails> CREATOR = new Creator<CupSecurePlusDetails>() {
        @Override
        public CupSecurePlusDetails createFromParcel(@NonNull Parcel source) {
            return new CupSecurePlusDetails(source);
        }

        @Override
        public CupSecurePlusDetails[] newArray(int size) {
            return new CupSecurePlusDetails[size];
        }
    };

    @NonNull
    public static final String KEY_SMS_CODE = "cupsecureplus.smscode";

    private String mSmsCode;

    private String mMd;

    private CupSecurePlusDetails() {
        // Empty constructor for Builder.
    }

    private CupSecurePlusDetails(@NonNull Parcel source) {
        mSmsCode = source.readString();
        mMd = source.readString();
    }

    @Override
    protected void finalize(@NonNull PaymentInitiationResponse.DetailFields detailFields) throws CheckoutException {
        ThreeDSecureRedirectData threeDSecureRedirectData = detailFields.getRedirectData(ThreeDSecureRedirectData.class);
        mMd = threeDSecureRedirectData.getMd();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mSmsCode);
        dest.writeString(mMd);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_SMS_CODE, mSmsCode);
        jsonObject.put("MD", mMd);

        return jsonObject;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CupSecurePlusDetails that = (CupSecurePlusDetails) o;

        if (mSmsCode != null ? !mSmsCode.equals(that.mSmsCode) : that.mSmsCode != null) {
            return false;
        }
        return mMd != null ? mMd.equals(that.mMd) : that.mMd == null;
    }

    @Override
    public int hashCode() {
        int result = mSmsCode != null ? mSmsCode.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mMd != null ? mMd.hashCode() : 0);
        return result;
    }

    public static final class Builder {
        private final CupSecurePlusDetails mCupSecurePlusDetails;

        public Builder(@NonNull String smsCode) {
            mCupSecurePlusDetails = new CupSecurePlusDetails();
            mCupSecurePlusDetails.mSmsCode = smsCode;
        }

        @NonNull
        public CupSecurePlusDetails build() {
            return mCupSecurePlusDetails;
        }
    }
}
