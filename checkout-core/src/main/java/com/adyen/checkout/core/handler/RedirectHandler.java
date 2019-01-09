/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 13/07/2018.
 */

package com.adyen.checkout.core.handler;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.RedirectDetails;

public interface RedirectHandler {
    /**
     * Called when a redirect is required to continue with the payment.
     *
     * @param redirectDetails The details of the redirect.
     */
    void onRedirectRequired(@NonNull RedirectDetails redirectDetails);
}
