/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 16/11/2018.
 */

package com.adyen.checkout.core.handler;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.AuthenticationDetails;

public interface AuthenticationHandler {
    /**
     * Called when authentication details are required to continue with the payment.
     *
     * @param authenticationDetails The required authentication details.
     */
    void onAuthenticationDetailsRequired(@NonNull AuthenticationDetails authenticationDetails);
}
