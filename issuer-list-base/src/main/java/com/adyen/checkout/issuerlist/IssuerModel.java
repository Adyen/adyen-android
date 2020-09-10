/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.model.paymentmethods.Item;


public final class IssuerModel {

    private final String mId;
    private final String mName;

    /**
     * Creates an IssuerModel object based on the parsed Item from the Payment Details.
     *
     * @param item The item source for the IssuerModel.
     */
    public IssuerModel(@NonNull Item item) {
        if (item.getId() == null || item.getName() == null) {
            throw new IllegalArgumentException("Item should not have null values.");
        }

        mId = item.getId();
        mName = item.getName();
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        return mName;
    }

}
