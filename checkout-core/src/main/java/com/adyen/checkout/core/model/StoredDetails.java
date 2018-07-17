package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 28/12/2017.
 */
public interface StoredDetails extends Parcelable {
    /**
     * @return The stored {@link Card}.
     */
    @Nullable
    Card getCard();

    /**
     * @return The stored email address.
     */
    @Nullable
    String getEmailAddress();
}
