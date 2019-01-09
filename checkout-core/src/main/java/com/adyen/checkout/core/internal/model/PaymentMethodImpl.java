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

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.model.Configuration;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class PaymentMethodImpl extends PaymentMethodBase implements PaymentMethod {
    @NonNull
    public static final Parcelable.Creator<PaymentMethodImpl> CREATOR = new DefaultCreator<>(PaymentMethodImpl.class);

    private static final String KEY_NAME = "name";

    private static final String KEY_INPUT_DETAILS = "details";

    private static final String KEY_CONFIGURATION = "configuration";

    private static final String KEY_GROUP = "group";

    private static final String KEY_STORED_DETAILS = "storedDetails";

    private static final String KEY_PAYMENT_METHOD_DATA = "paymentMethodData";

    private String mName;

    private List<InputDetailImpl> mInputDetails;

    private JSONObject mConfiguration;

    private PaymentMethodImpl mGroup;

    private StoredDetailsImpl mStoredDetails;

    private String mPaymentMethodData;

    @Nullable
    public static PaymentMethod findByType(@Nullable List<PaymentMethod> paymentMethods, @NonNull String type) {
        if (paymentMethods != null) {
            for (PaymentMethod paymentMethod : paymentMethods) {
                PaymentMethod toCheck = paymentMethod;

                do {
                    if (toCheck.getType().equals(type)) {
                        return toCheck;
                    } else {
                        toCheck = toCheck.getGroup();
                    }
                } while (toCheck != null);
            }
        }

        return null;
    }

    private PaymentMethodImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mName = jsonObject.getString(KEY_NAME);
        mInputDetails = parseOptionalList(KEY_INPUT_DETAILS, InputDetailImpl.class);
        mConfiguration = jsonObject.optJSONObject(KEY_CONFIGURATION);
        mGroup = parseOptional(KEY_GROUP, PaymentMethodImpl.class);
        mStoredDetails = parseOptional(KEY_STORED_DETAILS, StoredDetailsImpl.class);
        mPaymentMethodData = jsonObject.getString(KEY_PAYMENT_METHOD_DATA);
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @Nullable
    @Override
    public List<InputDetail> getInputDetails() {
        return mInputDetails != null ? new ArrayList<InputDetail>(mInputDetails) :  null;
    }

    @NonNull
    @Override
    public <T extends Configuration> T getConfiguration(@NonNull Class<T> clazz) throws CheckoutException {
        if (mConfiguration == null) {
            throw new CheckoutException.Builder("No Configuration is available.", null).build();
        }

        return ProvidedBy.Util.parse(mConfiguration, clazz);
    }

    @Nullable
    @Override
    public PaymentMethodImpl getGroup() {
        return mGroup;
    }

    @Nullable
    @Override
    public StoredDetailsImpl getStoredDetails() {
        return mStoredDetails;
    }

    @NonNull
    @Override
    public String getTxVariant() {
        return getType();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        PaymentMethodImpl that = (PaymentMethodImpl) o;

        if (mName != null ? !mName.equals(that.mName) : that.mName != null) {
            return false;
        }
        if (mPaymentMethodData != null ? !mPaymentMethodData.equals(that.mPaymentMethodData) : that.mPaymentMethodData != null) {
            return false;
        }
        if (mInputDetails != null ? !mInputDetails.equals(that.mInputDetails) : that.mInputDetails != null) {
            return false;
        }
        if (mConfiguration != null ? !mConfiguration.equals(that.mConfiguration) : that.mConfiguration != null) {
            return false;
        }
        if (mGroup != null ? !mGroup.equals(that.mGroup) : that.mGroup != null) {
            return false;
        }
        return mStoredDetails != null ? mStoredDetails.equals(that.mStoredDetails) : that.mStoredDetails == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtils.MULTIPLIER * result + (mName != null ? mName.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mPaymentMethodData != null ? mPaymentMethodData.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mInputDetails != null ? mInputDetails.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mConfiguration != null ? mConfiguration.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mGroup != null ? mGroup.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mStoredDetails != null ? mStoredDetails.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "PaymentMethod{" + "Name='" + mName + '\'' + '}';
    }

    @NonNull
    public String getPaymentMethodData() {
        return mPaymentMethodData;
    }
}
