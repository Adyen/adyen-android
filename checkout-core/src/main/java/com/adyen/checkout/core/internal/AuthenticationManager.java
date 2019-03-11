/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 16/11/2018.
 */

package com.adyen.checkout.core.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.AuthenticationDetails;
import com.adyen.checkout.core.handler.AuthenticationHandler;

final class AuthenticationManager extends BaseManager<AuthenticationHandler, AuthenticationDetails> {

    AuthenticationManager(@NonNull Listener listener) {
        super(listener);
    }

    @Override
    void dispatch(@NonNull AuthenticationHandler handler, @NonNull AuthenticationDetails data) {
        handler.onAuthenticationDetailsRequired(data);
    }
}
