/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 04/08/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.TxVariantProvider;
import com.adyen.checkout.core.CheckoutException;

import java.util.List;

public interface PaymentMethod extends TxVariantProvider, Parcelable {
    /**
     * @return The display name of the {@link PaymentMethod}.
     */
    @NonNull
    String getName();

    /**
     * @return The type of the {@link PaymentMethod}.
     */
    @NonNull
    String getType();

    /**
     * @return The {@link InputDetail InputDetails} for this {@link PaymentMethod}.
     */
    @Nullable
    List<InputDetail> getInputDetails();

    /**
     * Get the {@link Configuration} for this {@link PaymentMethod}.
     *
     * @param clazz The {@link Configuration} {@link Class}.
     * @param <T> The {@link Configuration} type.
     * @return The {@link Configuration}.
     * @throws CheckoutException If the data does not match the provided {@link Configuration} {@link Class}.
     */
    @NonNull
    <T extends Configuration> T getConfiguration(@NonNull Class<T> clazz) throws CheckoutException;

    /**
     * @return The {@link PaymentMethod} under which this {@link PaymentMethod} should be grouped.
     */
    @Nullable
    PaymentMethod getGroup();

    /**
     * @return The {@link StoredDetails} for this {@link PaymentMethod}.
     */
    @Nullable
    StoredDetails getStoredDetails();
}
