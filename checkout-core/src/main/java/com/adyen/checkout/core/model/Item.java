package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.TxSubVariantProvider;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 05/08/2017.
 */
public interface Item extends TxSubVariantProvider, Parcelable {
    /**
     * @return The ID of the {@link Item}.
     */
    @NonNull
    String getId();

    /**
     * @return The display name of the {@link Item}.
     */
    @NonNull
    String getName();
}
