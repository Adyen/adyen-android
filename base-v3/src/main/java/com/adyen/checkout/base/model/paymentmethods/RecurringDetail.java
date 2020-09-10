/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/10/2019.
 */

package com.adyen.checkout.base.model.paymentmethods;

import androidx.annotation.NonNull;

import org.json.JSONObject;

/**
 * @deprecated This class was deprecated since the content was actually from the {@link StoredPaymentMethod}.
 *             We are not using the RecurringDetail object from the `oneClickPaymentMethods`, we use `storedPaymentMethods` instead.
 */
@Deprecated
public class RecurringDetail extends StoredPaymentMethod {

    // TODO make StoredPaymentMethod when removing this class.

    @NonNull
    public static final Creator<RecurringDetail> CREATOR = new Creator<>(RecurringDetail.class);

    @NonNull
    public static final Serializer<StoredPaymentMethod> SERIALIZER = new Serializer<StoredPaymentMethod>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull StoredPaymentMethod modelObject) {
            // Get parameters from parent class
            return StoredPaymentMethod.SERIALIZER.serialize(modelObject);
        }

        @Override
        @NonNull
        public RecurringDetail deserialize(@NonNull JSONObject jsonObject) {
            final RecurringDetail recurringDetail = new RecurringDetail();

            // getting parameters from parent class
            final StoredPaymentMethod storedPaymentMethod = StoredPaymentMethod.SERIALIZER.deserialize(jsonObject);
            recurringDetail.setConfiguration(storedPaymentMethod.getConfiguration());
            recurringDetail.setDetails(storedPaymentMethod.getDetails());
            recurringDetail.setGroup(storedPaymentMethod.getGroup());
            recurringDetail.setName(storedPaymentMethod.getName());
            recurringDetail.setPaymentMethodData(storedPaymentMethod.getPaymentMethodData());
            recurringDetail.setSupportsRecurring(storedPaymentMethod.getSupportsRecurring());
            recurringDetail.setType(storedPaymentMethod.getType());

            recurringDetail.setBrand(storedPaymentMethod.getBrand());
            recurringDetail.setExpiryMonth(storedPaymentMethod.getExpiryMonth());
            recurringDetail.setExpiryYear(storedPaymentMethod.getExpiryYear());
            recurringDetail.setHolderName(storedPaymentMethod.getHolderName());
            recurringDetail.setId(storedPaymentMethod.getId());
            recurringDetail.setLastFour(storedPaymentMethod.getLastFour());
            recurringDetail.setShopperEmail(storedPaymentMethod.getShopperEmail());

            recurringDetail.setSupportedShopperInteractions(storedPaymentMethod.getSupportedShopperInteractions());

            return recurringDetail;
        }
    };
}
