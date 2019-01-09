/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 10/04/2018.
 */

package com.adyen.checkout.ui.internal.issuer;

import android.app.Application;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ObjectsCompat;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.IssuerDetails;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.model.CheckoutHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.util.RedirectUtil;
import com.adyen.checkout.ui.internal.common.util.image.Rembrandt;
import com.adyen.checkout.ui.internal.common.util.image.RequestArgs;

import java.util.concurrent.Callable;

abstract class IssuerCheckoutMethod extends CheckoutMethod {
    private IssuerCheckoutMethod(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        super(application, paymentMethod);
    }

    public static final class Default extends IssuerCheckoutMethod {
        Default(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            super(application, paymentMethod);
        }

        @Override
        public void onSelected(@NonNull CheckoutHandler checkoutHandler) {
            PaymentReference paymentReference = checkoutHandler.getPaymentReference();
            PaymentMethodHandler paymentMethodHandler = new IssuerHandler(paymentReference, getPaymentMethod());
            checkoutHandler.handleWithPaymentMethodHandler(paymentMethodHandler);
        }
    }

    public static final class InstalledApp extends IssuerCheckoutMethod {
        private final ResolveInfo mResolveInfo;

        private final Item mItem;

        @Nullable
        public static InstalledApp init(
                @NonNull Application application,
                @NonNull PaymentMethod paymentMethod,
                @NonNull RedirectUtil.ResolveResult resolveResult,
                @NonNull Item item
        ) {
            ResolveInfo resolveInfo = resolveResult.getResolveInfo();

            if (resolveResult.getResolveType() == RedirectUtil.ResolveType.APPLICATION && resolveInfo != null) {
                return new InstalledApp(application, paymentMethod, resolveInfo, item);
            } else {
                return null;
            }
        }

        private InstalledApp(
                @NonNull Application application,
                @NonNull PaymentMethod paymentMethod,
                @NonNull ResolveInfo resolveInfo,
                @NonNull Item item
        ) {
            super(application, paymentMethod);

            mResolveInfo = resolveInfo;
            mItem = item;
        }

        @Override
        public boolean isSameItem(@NonNull CheckoutMethod newItem) {
            return super.isSameItem(newItem) && newItem instanceof InstalledApp && ObjectsCompat.equals(mItem, ((InstalledApp) newItem).mItem);
        }

        @NonNull
        @Override
        public RequestArgs buildLogoRequestArgs(@NonNull LogoApi logoApi) {
            final Application application = getApplication();

            return Rembrandt.get(application)
                    .load(new AppIconCallable(application, mResolveInfo))
                    .placeholder(R.drawable.ic_image_24dp)
                    .error(R.drawable.ic_broken_image_24dp)
                    .build();
        }

        @NonNull
        @Override
        public String getSecondaryText() {
            Application application = getApplication();
            CharSequence appName = mResolveInfo.loadLabel(application.getPackageManager());

            return application.getString(R.string.checkout_issuer_open_with_app_format, appName);
        }

        @Override
        public void onSelected(@NonNull CheckoutHandler checkoutHandler) {
            IssuerDetails issuerDetails = new IssuerDetails.Builder(mItem.getId()).build();
            checkoutHandler.getPaymentHandler().initiatePayment(getPaymentMethod(), issuerDetails);
        }
    }

    private static final class AppIconCallable implements Callable<Drawable> {
        private final Application mApplication;

        private final ResolveInfo mResolveInfo;

        private AppIconCallable(@NonNull Application application, @NonNull ResolveInfo resolveInfo) {
            mApplication = application;
            mResolveInfo = resolveInfo;
        }

        @Override
        public Drawable call() {
            return mResolveInfo.loadIcon(mApplication.getPackageManager());
        }
    }
}
