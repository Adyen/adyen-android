package com.adyen.checkout.ui.internal.issuer;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.IssuerDetails;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodFactory;
import com.adyen.checkout.ui.internal.common.util.RedirectUtil;
import com.adyen.checkout.util.PaymentMethodTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 21/03/2018.
 */
public class IssuerCheckoutMethodFactory extends CheckoutMethodFactory {
    private static final Map<String, String> IDEAL_ISSUER_APP_PACKAGE_NAMES = new HashMap<String, String>() {
        {
            put("ING", "com.ing.mobile");
            put("Triodos Bank", "com.triodos.ib.mobile");
            put("ASN Bank", "nl.asnbank.asnbankieren");
            put("SNS Bank", "nl.snsbank.snsbankieren");
            put("RegioBank", "nl.regiobank.regiobankiere");
            put("ABN Amro", "com.abnamro.nl.mobile.payments");
            put("Rabobank", "nl.rabomobiel");
            put("bunq", "com.bunq.android");
            put("Knab", "bvm.bvmapp");
        }
    };

    public IssuerCheckoutMethodFactory(@NonNull Application application) {
        super(application);
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initOneClickCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final List<CheckoutMethod> checkoutMethods = new ArrayList<>();

        PaymentMethod paymentMethod = PaymentMethodImpl.findByType(paymentSession.getPaymentMethods(), PaymentMethodTypes.IDEAL);
        InputDetail inputDetail = paymentMethod != null ? InputDetailImpl.findByKey(paymentMethod.getInputDetails(), IssuerDetails.KEY_ISSUER) : null;
        List<Item> items = inputDetail != null ? inputDetail.getItems() : null;

        if (items != null) {
            Application application = getApplication();

            for (Item item : items) {
                String packageName = IDEAL_ISSUER_APP_PACKAGE_NAMES.get(item.getName());
                RedirectUtil.ResolveResult resolveResult = packageName != null
                        ? RedirectUtil.determineResolveResult(application, packageName)
                        : null;

                if (resolveResult != null && resolveResult.getResolveType() == RedirectUtil.ResolveType.APPLICATION) {
                    IssuerCheckoutMethod.InstalledApp installedApp = IssuerCheckoutMethod.InstalledApp
                            .init(application, paymentMethod, resolveResult, item);

                    if (installedApp != null) {
                        checkoutMethods.add(installedApp);
                    }
                }
            }
        }

        return new Callable<List<CheckoutMethod>>() {
            @Override
            public List<CheckoutMethod> call() {
                return checkoutMethods;
            }
        };
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initCheckoutMethods(@NonNull PaymentSession paymentSession) {
        final List<CheckoutMethod> checkoutMethods = new ArrayList<>();
        List<PaymentMethod> paymentMethods = paymentSession.getPaymentMethods();

        Application application = getApplication();

        for (PaymentMethod paymentMethod : paymentMethods) {
            if (IssuerHandler.FACTORY.supports(application, paymentMethod)
                    && IssuerHandler.FACTORY.isAvailableToShopper(application, paymentSession, paymentMethod)) {
                checkoutMethods.add(new IssuerCheckoutMethod.Default(application, paymentMethod));
            }
        }

        return new Callable<List<CheckoutMethod>>() {
            @Override
            public List<CheckoutMethod> call() {
                return checkoutMethods;
            }
        };
    }
}
