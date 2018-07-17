package com.adyen.checkout.ui.internal.common.model;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.model.PaymentSession;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 20/03/2018.
 */
public abstract class CheckoutMethodFactory {
    private Application mApplication;

    public CheckoutMethodFactory(@NonNull Application application) {
        mApplication = application;
    }

    @NonNull
    public Application getApplication() {
        return mApplication;
    }

    @Nullable
    public abstract Callable<List<CheckoutMethod>> initOneClickCheckoutMethods(@NonNull PaymentSession paymentSession);

    @Nullable
    public abstract Callable<List<CheckoutMethod>> initCheckoutMethods(@NonNull PaymentSession paymentSession);
}
