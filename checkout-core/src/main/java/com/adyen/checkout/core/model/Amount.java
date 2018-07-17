package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 05/08/2017.
 */
public interface Amount extends Parcelable {
    /**
     * @return The value of the {@link Amount} in minor units.
     */
    long getValue();

    /**
     * @return The ISO 4217 currency code of the {@link Amount}.
     */
    @NonNull
    String getCurrency();
}
