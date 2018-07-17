package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.model.Configuration;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/07/2018.
 */
public final class PaymentMethodImpl extends PaymentMethodBase implements PaymentMethod {
    public static final Parcelable.Creator<PaymentMethodImpl> CREATOR = new DefaultCreator<>(PaymentMethodImpl.class);

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

    private String mName;

    private List<InputDetailImpl> mInputDetails;

    private JSONObject mConfiguration;

    private PaymentMethodImpl mGroup;

    private StoredDetailsImpl mStoredDetails;

    private String mPaymentMethodData;

    private PaymentMethodImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mName = jsonObject.getString("name");
        mInputDetails = parseOptionalList("details", InputDetailImpl.class);
        mConfiguration = jsonObject.optJSONObject("configuration");
        mGroup = parseOptional("group", PaymentMethodImpl.class);
        mStoredDetails = parseOptional("storedDetails", StoredDetailsImpl.class);
        mPaymentMethodData = jsonObject.getString("paymentMethodData");
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @Nullable
    @Override
    public List<InputDetail> getInputDetails() {
        return mInputDetails != null ? new ArrayList<InputDetail>(mInputDetails) : null;
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
    public boolean equals(Object o) {
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
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mPaymentMethodData != null ? mPaymentMethodData.hashCode() : 0);
        result = 31 * result + (mInputDetails != null ? mInputDetails.hashCode() : 0);
        result = 31 * result + (mConfiguration != null ? mConfiguration.hashCode() : 0);
        result = 31 * result + (mGroup != null ? mGroup.hashCode() : 0);
        result = 31 * result + (mStoredDetails != null ? mStoredDetails.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PaymentMethod{" + "Name='" + mName + '\'' + '}';
    }

    @NonNull
    public String getPaymentMethodData() {
        return mPaymentMethodData;
    }
}
