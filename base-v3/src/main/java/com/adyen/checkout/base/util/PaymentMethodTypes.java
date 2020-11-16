/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.base.util;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class with a list of all the currently supported Payment Methods on Components and Drop-In.
 */
@SuppressWarnings("SpellCheckingInspection")
public final class PaymentMethodTypes {

    // Placeholder value if the type is not found.
    public static final String UNKNOWN = "unknown";

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
    public static final String AFTER_PAY = "afterpay_default";
    public static final String BCMC = "bcmc";
    public static final String WECHAT_PAY_SDK = "wechatpaySDK";
    public static final String MB_WAY = "mbway";

    // Voucher payment methods that are not yet supported
    public static final String MULTIBANCO = "multibanco";
    public static final String OXXO = "oxxo";

    public static final String DOKU = "doku";
    public static final String DOKU_ALFMART = "doku_alfamart";
    public static final String DOKU_PERMATA_LITE_ATM = "doku_permata_lite_atm";
    public static final String DOKU_INDOMARET = "doku_indomaret";
    public static final String DOKU_ATM_MANDIRI_VA = "doku_atm_mandiri_va";
    public static final String DOKU_SINARMAS_VA = "doku_sinarmas_va";
    public static final String DOKU_MANDIRI_VA = "doku_mandiri_va";
    public static final String DOKU_CIMB_VA = "doku_cimb_va";
    public static final String DOKU_DANAMON_VA = "doku_danamon_va";
    public static final String DOKU_BRI_VA = "doku_bri_va";
    public static final String DOKU_BNI_VA = "doku_bni_va";
    public static final String DOKU_BCA_VA = "doku_bca_va";
    public static final String DOKU_WALLET = "doku_wallet";

    public static final String BOLETOBANCARIO = "boletobancario";
    public static final String BOLETOBANCARIO_BANCODOBRASIL = "boletobancario_bancodobrasil";
    public static final String BOLETOBANCARIO_BRADESCO = "boletobancario_bradesco";
    public static final String BOLETOBANCARIO_HSBC = "boletobancario_hsbc";
    public static final String BOLETOBANCARIO_ITAU = "boletobancario_itau";
    public static final String BOLETOBANCARIO_SANTANDER = "boletobancario_santander";

    public static final String DRAGONPAY_EBANKING = "dragonpay_ebanking";
    public static final String DRAGONPAY_OTC_BANKING = "dragonpay_otc_banking";
    public static final String DRAGONPAY_OTC_NON_BANKING = "dragonpay_otc_non_banking";
    public static final String DRAGONPAY_OTC_PHILIPPINES = "dragonpay_otc_philippines";

    public static final String ECONTEXT_SEVEN_ELEVEN = "econtext_seven_eleven";
    public static final String ECONTEXT_ATM = "econtext_atm";
    public static final String ECONTEXT_STORES = "econtext_stores";
    public static final String ECONTEXT_ONLINE = "econtext_online";

    // Payment methods that might be interpreted as redirect, but are actually not supported
    public static final String BCMC_QR = "bcmc_mobile_QR";
    public static final String WECHAT_PAY_MINI_PROGRAM = "wechatpayMiniProgram";
    public static final String WECHAT_PAY_QR = "wechatpayQR";
    public static final String WECHAT_PAY_WEB = "wechatpayWeb";

    // List of all payment method types.
    public static final List<String> SUPPORTED_PAYMENT_METHODS;
    public static final List<String> UNSUPPORTED_PAYMENT_METHODS;

    static {
        final ArrayList<String> supportedPaymentMethods = new ArrayList<>();

        // Populate supported list
        supportedPaymentMethods.add(AFTER_PAY);
        supportedPaymentMethods.add(BCMC);
        supportedPaymentMethods.add(DOTPAY);
        supportedPaymentMethods.add(ENTERCASH);
        supportedPaymentMethods.add(EPS);
        supportedPaymentMethods.add(GOOGLE_PAY);
        supportedPaymentMethods.add(IDEAL);
        supportedPaymentMethods.add(MB_WAY);
        supportedPaymentMethods.add(MOLPAY_MALAYSIA);
        supportedPaymentMethods.add(MOLPAY_THAILAND);
        supportedPaymentMethods.add(MOLPAY_VIETNAM);
        supportedPaymentMethods.add(OPEN_BANKING);
        // Sepa is not supported until we fix the Component
        //    supportedPaymentMethods.add(SEPA);
        supportedPaymentMethods.add(SCHEME);
        supportedPaymentMethods.add(WECHAT_PAY_SDK);

        SUPPORTED_PAYMENT_METHODS = Collections.unmodifiableList(supportedPaymentMethods);

        final ArrayList<String> unsupportedPaymentMethods = new ArrayList<>();

        // Populate unsupported list

        // Sepa is not supported until we fix the Component
        unsupportedPaymentMethods.add(SEPA);

        unsupportedPaymentMethods.add(BCMC_QR);
        unsupportedPaymentMethods.add(WECHAT_PAY_MINI_PROGRAM);
        unsupportedPaymentMethods.add(WECHAT_PAY_QR);
        unsupportedPaymentMethods.add(WECHAT_PAY_WEB);

        unsupportedPaymentMethods.add(MULTIBANCO);
        unsupportedPaymentMethods.add(OXXO);
        unsupportedPaymentMethods.add(DOKU);
        unsupportedPaymentMethods.add(DOKU_ALFMART);
        unsupportedPaymentMethods.add(DOKU_PERMATA_LITE_ATM);
        unsupportedPaymentMethods.add(DOKU_INDOMARET);
        unsupportedPaymentMethods.add(DOKU_ATM_MANDIRI_VA);
        unsupportedPaymentMethods.add(DOKU_SINARMAS_VA);
        unsupportedPaymentMethods.add(DOKU_MANDIRI_VA);
        unsupportedPaymentMethods.add(DOKU_CIMB_VA);
        unsupportedPaymentMethods.add(DOKU_DANAMON_VA);
        unsupportedPaymentMethods.add(DOKU_BRI_VA);
        unsupportedPaymentMethods.add(DOKU_BNI_VA);
        unsupportedPaymentMethods.add(DOKU_BCA_VA);
        unsupportedPaymentMethods.add(DOKU_WALLET);

        unsupportedPaymentMethods.add(BOLETOBANCARIO);
        unsupportedPaymentMethods.add(BOLETOBANCARIO_BANCODOBRASIL);
        unsupportedPaymentMethods.add(BOLETOBANCARIO_BRADESCO);
        unsupportedPaymentMethods.add(BOLETOBANCARIO_HSBC);
        unsupportedPaymentMethods.add(BOLETOBANCARIO_ITAU);
        unsupportedPaymentMethods.add(BOLETOBANCARIO_SANTANDER);

        unsupportedPaymentMethods.add(DRAGONPAY_EBANKING);
        unsupportedPaymentMethods.add(DRAGONPAY_OTC_BANKING);
        unsupportedPaymentMethods.add(DRAGONPAY_OTC_NON_BANKING);
        unsupportedPaymentMethods.add(DRAGONPAY_OTC_PHILIPPINES);

        unsupportedPaymentMethods.add(ECONTEXT_SEVEN_ELEVEN);
        unsupportedPaymentMethods.add(ECONTEXT_ATM);
        unsupportedPaymentMethods.add(ECONTEXT_STORES);
        unsupportedPaymentMethods.add(ECONTEXT_ONLINE);

        UNSUPPORTED_PAYMENT_METHODS = Collections.unmodifiableList(unsupportedPaymentMethods);
    }

    private PaymentMethodTypes() {
        throw new NoConstructorException();
    }
}
