/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */

package com.adyen.checkout.base.model.paymentmethods;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.exeption.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public final class RecurringDetail extends PaymentMethod {
    @NonNull
    public static final Creator<RecurringDetail> CREATOR = new Creator<>(RecurringDetail.class);

    private static final String RECURRING_DETAIL_REFERENCE = "recurringDetailReference";
    private static final String STORED_DETAILS = "storedDetails";

    @NonNull
    public static final Serializer<RecurringDetail> SERIALIZER = new Serializer<RecurringDetail>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull RecurringDetail modelObject) {
            // Get parameters from parent class
            final JSONObject jsonObject = PaymentMethod.SERIALIZER.serialize(modelObject);
            try {
                jsonObject.putOpt(RECURRING_DETAIL_REFERENCE, modelObject.getRecurringDetailReference());
                jsonObject.putOpt(STORED_DETAILS, ModelUtils.serializeOpt(modelObject.getStoredDetails(), StoredDetails.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(RecurringDetail.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public RecurringDetail deserialize(@NonNull JSONObject jsonObject) {
            final RecurringDetail recurringDetail = new RecurringDetail();

            // getting parameters from parent class
            final PaymentMethod paymentMethod = PaymentMethod.SERIALIZER.deserialize(jsonObject);
            recurringDetail.setConfiguration(paymentMethod.getConfiguration());
            recurringDetail.setDetails(paymentMethod.getDetails());
            recurringDetail.setGroup(paymentMethod.getGroup());
            recurringDetail.setName(paymentMethod.getName());
            recurringDetail.setPaymentMethodData(paymentMethod.getPaymentMethodData());
            recurringDetail.setSupportsRecurring(paymentMethod.getSupportsRecurring());
            recurringDetail.setType(paymentMethod.getType());

            recurringDetail.setRecurringDetailReference(jsonObject.optString(RECURRING_DETAIL_REFERENCE, null));
            recurringDetail.setStoredDetails(ModelUtils.deserializeOpt(jsonObject.optJSONObject(STORED_DETAILS), StoredDetails.SERIALIZER));

            return recurringDetail;
        }
    };

    private String recurringDetailReference;
    private StoredDetails storedDetails;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getRecurringDetailReference() {
        return recurringDetailReference;
    }

    @Nullable
    public StoredDetails getStoredDetails() {
        return storedDetails;
    }

    public void setRecurringDetailReference(@Nullable String recurringDetailReference) {
        this.recurringDetailReference = recurringDetailReference;
    }

    public void setStoredDetails(@Nullable StoredDetails storedDetails) {
        this.storedDetails = storedDetails;
    }
}
