/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.base.util;

import android.support.annotation.StringDef;

import com.adyen.checkout.core.exception.NoConstructorException;

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
    public static final String MOLPAY_MALAYSIA = "molpay_ebanking_fpx_MY";
    public static final String MOLPAY_THAILAND = "molpay_ebanking_TH";
    public static final String MOLPAY_VIETNAM = "molpay_ebanking_VN";
    public static final String DOTPAY = "dotpay";
    public static final String EPS = "eps";
    public static final String ENTERCASH = "entercash";
    public static final String OPEN_BANKING = "openbanking_UK";
    public static final String SCHEME = "scheme";
    public static final String GOOGLE_PAY = "paywithgoogle";
    public static final String SEPA = "sepadirectdebit";
    public static final String BCMC = "bcmc";
    public static final String WECHAT_PAY_SDK = "wechatpaySDK";

    // Payment methods that might be interpreted as redirect, but are actually not supported
    public static final String BCMC_QR = "bcmc_mobile_QR";
    public static final String WECHAT_PAY_MINI_PROGRAM = "wechatpayMiniProgram";
    public static final String WECHAT_PAY_QR = "wechatpayQR";
    public static final String WECHAT_PAY_WEB = "wechatpayWeb";

    // List of all payment method types.
    public static final List<String> SUPPORTED_PAYMENT_METHODS;
    public static final List<String> UNSUPPORTED_PAYMENT_METHODS;

    // Helper annotation to enforce use of a constant from here when needed.
    @StringDef({IDEAL, MOLPAY_MALAYSIA, MOLPAY_THAILAND, MOLPAY_VIETNAM, DOTPAY, EPS, ENTERCASH, OPEN_BANKING, SCHEME, GOOGLE_PAY, SEPA, BCMC,
            WECHAT_PAY_SDK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SupportedPaymentMethod {
    }

    static {
        final ArrayList<String> supportedPaymentMethods = new ArrayList<>();

        // Populate supported list
        supportedPaymentMethods.add(IDEAL);
        supportedPaymentMethods.add(MOLPAY_MALAYSIA);
        supportedPaymentMethods.add(MOLPAY_THAILAND);
        supportedPaymentMethods.add(MOLPAY_VIETNAM);
        supportedPaymentMethods.add(DOTPAY);
        supportedPaymentMethods.add(EPS);
        supportedPaymentMethods.add(ENTERCASH);
        supportedPaymentMethods.add(OPEN_BANKING);
        supportedPaymentMethods.add(SCHEME);
        supportedPaymentMethods.add(GOOGLE_PAY);
        supportedPaymentMethods.add(SEPA);
        supportedPaymentMethods.add(BCMC);
        supportedPaymentMethods.add(WECHAT_PAY_SDK);

        SUPPORTED_PAYMENT_METHODS = Collections.unmodifiableList(supportedPaymentMethods);

        final ArrayList<String> unsupportedPaymentMethods = new ArrayList<>();

        // Populate unsupported list
        unsupportedPaymentMethods.add(BCMC_QR);
        unsupportedPaymentMethods.add(WECHAT_PAY_MINI_PROGRAM);
        unsupportedPaymentMethods.add(WECHAT_PAY_QR);
        unsupportedPaymentMethods.add(WECHAT_PAY_WEB);

        UNSUPPORTED_PAYMENT_METHODS = Collections.unmodifiableList(unsupportedPaymentMethods);
    }

    private PaymentMethodTypes() {
        throw new NoConstructorException();
    }
}
