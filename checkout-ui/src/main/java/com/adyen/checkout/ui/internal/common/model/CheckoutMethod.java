/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 16/03/2018.
 */

package com.adyen.checkout.ui.internal.common.model;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ObjectsCompat;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.internal.common.util.image.Rembrandt;
import com.adyen.checkout.ui.internal.common.util.image.RequestArgs;
import com.adyen.checkout.ui.internal.common.util.recyclerview.SimpleDiffCallback;

import java.util.concurrent.Callable;

public abstract class CheckoutMethod implements SimpleDiffCallback.Comparable<CheckoutMethod> {
    private final Application mApplication;

    private final PaymentMethod mPaymentMethod;

    public CheckoutMethod(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        mApplication = application;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public boolean isSameItem(@NonNull CheckoutMethod newItem) {
        return mPaymentMethod.equals(newItem.mPaymentMethod);
    }

    @Override
    public boolean isSameContent(@NonNull CheckoutMethod newItem) {
        return ObjectsCompat.equals(getPrimaryText(), newItem.getPrimaryText())
                && ObjectsCompat.equals(getSecondaryText(), newItem.getSecondaryText());
    }

    @NonNull
    public RequestArgs buildLogoRequestArgs(@NonNull LogoApi logoApi) {
        Callable<Drawable> logoCallable = logoApi.newBuilder(mPaymentMethod).buildCallable();

        return Rembrandt.createDefaultLogoRequestArgs(mApplication, logoCallable);
    }

    @NonNull
    public String getPrimaryText() {
        return mPaymentMethod.getName();
    }

    @Nullable
    public String getSecondaryText() {
        return null;
    }

    public abstract void onSelected(@NonNull CheckoutHandler checkoutHandler);

    @NonNull
    public final Application getApplication() {
        return mApplication;
    }

    @NonNull
    public final PaymentMethod getPaymentMethod() {
        return mPaymentMethod;
    }
}
