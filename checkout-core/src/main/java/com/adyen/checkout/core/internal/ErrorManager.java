package com.adyen.checkout.core.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.handler.ErrorHandler;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 13/07/2018.
 */
final class ErrorManager extends BaseManager<ErrorHandler, CheckoutException> {
    ErrorManager(@NonNull Listener listener) {
        super(listener);
    }

    @Override
    void dispatch(@NonNull ErrorHandler handler, @NonNull CheckoutException data) {
        handler.onError(data);
    }
}
