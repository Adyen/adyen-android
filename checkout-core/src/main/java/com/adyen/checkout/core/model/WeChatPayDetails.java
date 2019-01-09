/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 08/05/2018.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.internal.model.AdditionalPaymentMethodDetails;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.internal.model.PaymentInitiationResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link PaymentMethodDetails} for WeChat Pay payments.
 */
public final class WeChatPayDetails extends AdditionalPaymentMethodDetails {
    @NonNull
    public static final Creator<WeChatPayDetails> CREATOR = new Creator<WeChatPayDetails>() {
        @Override
        public WeChatPayDetails createFromParcel(Parcel source) {
            return new WeChatPayDetails(source);
        }

        @Override
        public WeChatPayDetails[] newArray(int size) {
            return new WeChatPayDetails[size];
        }
    };

    @NonNull
    public static final String KEY_RESULT_CODE = "resultCode";

    private static final String KEY_PAYMENT_METHOD_RETURN_DATA = "paymentMethodReturnData";

    private String mResultCode;

    private String mPaymentMethodReturnData;

    private WeChatPayDetails() {
        // Empty constructor for Builder.
    }

    private WeChatPayDetails(@NonNull Parcel in) {
        super(in);
        this.mResultCode = in.readString();
        this.mPaymentMethodReturnData = in.readString();
    }

    @Override
    protected void finalize(@NonNull PaymentInitiationResponse.DetailFields detailFields) {
        mPaymentMethodReturnData = detailFields.getPaymentMethodReturnData();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.mResultCode);
        dest.writeString(this.mPaymentMethodReturnData);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_RESULT_CODE, mResultCode);
        jsonObject.put(KEY_PAYMENT_METHOD_RETURN_DATA, mPaymentMethodReturnData);

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

        WeChatPayDetails that = (WeChatPayDetails) o;

        if (mResultCode != null ? !mResultCode.equals(that.mResultCode) : that.mResultCode != null) {
            return false;
        }
        return mPaymentMethodReturnData != null
                ? mPaymentMethodReturnData.equals(that.mPaymentMethodReturnData)
                : that.mPaymentMethodReturnData == null;
    }

    @Override
    public int hashCode() {
        int result = mResultCode != null ? mResultCode.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mPaymentMethodReturnData != null ? mPaymentMethodReturnData.hashCode() : 0);
        return result;
    }

    public static final class Builder {
        private final WeChatPayDetails mWeChatPayDetails;

        public Builder(@NonNull String resultCode) {
            mWeChatPayDetails = new WeChatPayDetails();
            mWeChatPayDetails.mResultCode = resultCode;
        }

        @NonNull
        public WeChatPayDetails build() {
            return mWeChatPayDetails;
        }
    }
}
