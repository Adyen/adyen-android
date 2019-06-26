/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.base.util;

import android.support.annotation.StringDef;

import com.adyen.checkout.core.exeption.NoConstructorException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class with a list of all the currently supported Payment Methods on Components and Drop-In.
 */
public final class PaymentMethodTypes {

    // Type of the payment method as received by the paymentMethods/ API
    public static final String IDEAL = "ideal";
    public static final String MOLPAY = "molpay_ebanking_fpx_MY";
    public static final String DOTPAY = "dotpay";
    public static final String EPS = "eps";
    public static final String ENTERCASH = "entercash";
    public static final String OPEN_BANKING = "openbanking_UK";
    public static final String SCHEME = "scheme";

    // List of all payment method types.
    public static final List<String> SUPPORTED_PAYMENT_METHODS;

    // Helper annotation to enforce use of a constant from here when needed.
    @StringDef({IDEAL, MOLPAY, DOTPAY, EPS, ENTERCASH, OPEN_BANKING, SCHEME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SupportedPaymentMethod {
    }

    static {
        final ArrayList<String> paymentMethods = new ArrayList<>();

        paymentMethods.add(IDEAL);
        paymentMethods.add(MOLPAY);
        paymentMethods.add(DOTPAY);
        paymentMethods.add(EPS);
        paymentMethods.add(ENTERCASH);
        paymentMethods.add(OPEN_BANKING);
        paymentMethods.add(SCHEME);

        SUPPORTED_PAYMENT_METHODS = Collections.unmodifiableList(paymentMethods);
    }

    private PaymentMethodTypes() {
        throw new NoConstructorException();
    }
}
