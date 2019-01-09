/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 17/04/2018.
 */

package com.adyen.checkout.ui.internal.common.model;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.card.CardCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.def.DefaultCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.doku.DokuCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.giropay.GiroPayCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.googlepay.GooglePayCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.issuer.IssuerCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.openinvoice.OpenInvoiceCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.paypal.PayPalCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.qiwiwallet.QiwiWalletCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.sepadirectdebit.SddCheckoutMethodFactory;
import com.adyen.checkout.ui.internal.wechatpay.WeChatPayCheckoutMethodFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public class UpdateCheckoutMethodsCallable implements Callable<CheckoutMethodsModel> {
    private static final String TAG = UpdateCheckoutMethodsCallable.class.getSimpleName();

    private final PaymentSession mPaymentSession;

    private final List<CheckoutMethodFactory> mCheckoutMethodFactories;

    private final DefaultCheckoutMethodFactory mDefaultCheckoutMethodFactory;

    private final boolean mIncludePreselectedCheckoutMethod;

    public UpdateCheckoutMethodsCallable(
            @NonNull Application application,
            @NonNull PaymentSession paymentSession,
            boolean includePreselectedCheckoutMethod
    ) {
        mPaymentSession = paymentSession;
        mCheckoutMethodFactories = initCheckoutMethodFactories(application);
        mDefaultCheckoutMethodFactory = new DefaultCheckoutMethodFactory(application);
        mIncludePreselectedCheckoutMethod = includePreselectedCheckoutMethod;
    }

    @Nullable
    @Override
    public CheckoutMethodsModel call() {
        // TODO: 17/04/2018 Execute multi-threaded?
        List<CheckoutMethod> oneClickCheckoutMethods = new ArrayList<>();
        List<CheckoutMethod> checkoutMethods = new ArrayList<>();

        for (CheckoutMethodFactory checkoutMethodFactory : mCheckoutMethodFactories) {
            callAndAddToList(checkoutMethodFactory.initOneClickCheckoutMethods(mPaymentSession), oneClickCheckoutMethods);
            callAndAddToList(checkoutMethodFactory.initCheckoutMethods(mPaymentSession), checkoutMethods);
        }

        mDefaultCheckoutMethodFactory.setHandledOneClickPaymentMethods(getHandledPaymentMethods(oneClickCheckoutMethods));
        mDefaultCheckoutMethodFactory.setHandledPaymentMethods(getHandledPaymentMethods(checkoutMethods));

        callAndAddToList(mDefaultCheckoutMethodFactory.initOneClickCheckoutMethods(mPaymentSession), oneClickCheckoutMethods);
        callAndAddToList(mDefaultCheckoutMethodFactory.initCheckoutMethods(mPaymentSession), checkoutMethods);

        Collections.sort(oneClickCheckoutMethods, new CheckoutMethodComparator(mPaymentSession.getOneClickPaymentMethods()));
        Collections.sort(checkoutMethods, new CheckoutMethodComparator(mPaymentSession.getPaymentMethods()));

        return CheckoutMethodsModel.build(oneClickCheckoutMethods, checkoutMethods, mIncludePreselectedCheckoutMethod);
    }

    private void callAndAddToList(@Nullable Callable<List<CheckoutMethod>> callable, @NonNull List<CheckoutMethod> list) {
        if (callable == null) {
            return;
        }

        try {
            List<CheckoutMethod> result = callable.call();

            if (result != null) {
                list.addAll(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating checkout methods.", e);
        }
    }

    @NonNull
    private List<PaymentMethod> getHandledPaymentMethods(@NonNull List<CheckoutMethod> checkoutMethods) {
        List<PaymentMethod> handledPaymentMethods = new ArrayList<>();

        for (CheckoutMethod checkoutMethod : checkoutMethods) {
            handledPaymentMethods.add(checkoutMethod.getPaymentMethod());
        }

        return handledPaymentMethods;
    }

    @NonNull
    private List<CheckoutMethodFactory> initCheckoutMethodFactories(@NonNull Application application) {
        List<CheckoutMethodFactory> checkoutMethodFactories = new ArrayList<>();

        checkoutMethodFactories.add(new CardCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new DokuCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new GiroPayCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new GooglePayCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new IssuerCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new OpenInvoiceCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new PayPalCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new QiwiWalletCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new SddCheckoutMethodFactory(application));
        checkoutMethodFactories.add(new WeChatPayCheckoutMethodFactory(application));

        return Collections.unmodifiableList(checkoutMethodFactories);
    }

    private static final class CheckoutMethodComparator implements Comparator<CheckoutMethod> {
        private final List<PaymentMethod> mPaymentMethods;

        private CheckoutMethodComparator(@Nullable List<PaymentMethod> paymentMethods) {
            mPaymentMethods = paymentMethods;
        }

        @Override
        public int compare(CheckoutMethod lhs, CheckoutMethod rhs) {
            int lhsIndex = getIndexOfPaymentMethod(lhs.getPaymentMethod());
            int rhsIndex = getIndexOfPaymentMethod(rhs.getPaymentMethod());

            //noinspection UseCompareMethod
            return (lhsIndex < rhsIndex) ? -1 : ((lhsIndex == rhsIndex) ? 0 : 1);
        }

        private int getIndexOfPaymentMethod(@NonNull PaymentMethod paymentMethod) {
            if (mPaymentMethods != null) {
                for (int i = 0; i < mPaymentMethods.size(); i++) {
                    PaymentMethod p = mPaymentMethods.get(i);

                    if (paymentMethod.equals(p) || paymentMethod.equals(p.getGroup())) {
                        return i;
                    }
                }
            }

            return Integer.MAX_VALUE;
        }
    }
}
