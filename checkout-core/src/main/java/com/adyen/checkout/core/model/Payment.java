package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 04/08/2017.
 */
public interface Payment extends Parcelable {
    /**
     * @return The {@link Amount} of the {@link Payment}.
     */
    @NonNull
    Amount getAmount();
}
