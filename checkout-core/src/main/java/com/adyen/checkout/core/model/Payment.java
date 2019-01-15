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

public interface Payment extends Parcelable {
    /**
     * @return The {@link Amount} of the {@link Payment}.
     */
    @NonNull
    Amount getAmount();

    /**
     * @return The code of the country specified for this Payment.
     */
    @NonNull
    String getCountryCode();
}
