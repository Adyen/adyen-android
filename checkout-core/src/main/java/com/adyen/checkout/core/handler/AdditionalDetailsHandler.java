package com.adyen.checkout.core.handler;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.AdditionalDetails;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 13/07/2018.
 */
public interface AdditionalDetailsHandler {
    /**
     * Called when additional details are required to continue with the payment.
     *
     * @param additionalDetails The required additional details.
     */
    void onAdditionalDetailsRequired(@NonNull AdditionalDetails additionalDetails);
}
