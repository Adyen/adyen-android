/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.model.paymentmethods.Item;

import java.util.List;

public final class IssuerModel {

    private final String mId;
    private final String mName;

    private Drawable mLogo;

    private boolean mIsUpdated;

    /**
     * Looks for an IssuerModel in a List by the ID.
     * @param id The ID of the IssuerModel.
     * @param list The list to be searched.
     * @return The corresponding IssuerModel, null if not found.
     */
    @Nullable
    public static IssuerModel getFromList(@NonNull String id, @Nullable List<IssuerModel> list) {
        if (list != null) {
            for (IssuerModel issuer : list) {
                if (id.equals(issuer.getId())) {
                    return issuer;
                }
            }
        }
        return null;
    }

    /**
     * Creates an IssuerModel object based on the parsed Item from the Payment Details.
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

    @Nullable
    public Drawable getLogo() {
        return mLogo;
    }

    public void setLogo(@NonNull Drawable logo) {
        mLogo = logo;
        mIsUpdated = true;
    }

    public boolean isUpdated() {
        return mIsUpdated;
    }

    public void consumeUpdate() {
        mIsUpdated = false;
    }

}
