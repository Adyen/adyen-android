package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/08/2017.
 */
public interface Card extends Parcelable {
    /**
     * @return The holder name of this {@link Card}.
     */
    @NonNull
    String getHolderName();

    /**
     * @return The expiry month of this {@link Card}, where January corresponds 1;
     */
    int getExpiryMonth();

    /**
     * @return The expiry year of this {@link Card}.
     */
    int getExpiryYear();

    /**
     * @return The last four digits of the card number.
     */
    @NonNull
    String getLastFourDigits();
}
