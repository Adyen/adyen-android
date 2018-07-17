package com.adyen.checkout.core.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.RedirectDetails;
import com.adyen.checkout.core.handler.RedirectHandler;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 13/07/2018.
 */
final class RedirectManager extends BaseManager<RedirectHandler, RedirectDetails> {
    RedirectManager(@NonNull Listener listener) {
        super(listener);
    }

    @Override
    void dispatch(@NonNull RedirectHandler handler, @NonNull RedirectDetails data) {
        handler.onRedirectRequired(data);
    }
}
