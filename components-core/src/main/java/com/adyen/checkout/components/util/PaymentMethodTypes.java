/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.components.util;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.Arrays;
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
    public static final String GOOGLE_PAY = "googlepay";
    public static final String GOOGLE_PAY_LEGACY = "paywithgoogle";
    public static final String SEPA = "sepadirectdebit";
    public static final String BACS = "directdebit_GB";
    public static final String BCMC = "bcmc";
    public static final String MB_WAY = "mbway";
    public static final String BLIK = "blik";
    public static final String GIFTCARD = "giftcard";

    // Payment methods that do not need a payment component, but only an action component
    public static final String WECHAT_PAY_SDK = "wechatpaySDK";
    public static final String PIX = "pix";

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
    public static final String AFTER_PAY = "afterpay_default";
    public static final String WECHAT_PAY_MINI_PROGRAM = "wechatpayMiniProgram";
    public static final String WECHAT_PAY_QR = "wechatpayQR";
    public static final String WECHAT_PAY_WEB = "wechatpayWeb";

    // List of all payment method types.
    public static final List<String> SUPPORTED_PAYMENT_METHODS;
    public static final List<String> SUPPORTED_ACTION_ONLY_PAYMENT_METHODS;
    public static final List<String> UNSUPPORTED_PAYMENT_METHODS;

    static {
        SUPPORTED_PAYMENT_METHODS = Collections.unmodifiableList(Arrays.asList(
                BCMC,
                DOTPAY,
                ENTERCASH,
                EPS,
                GIFTCARD,
                GOOGLE_PAY,
                GOOGLE_PAY_LEGACY,
                IDEAL,
                MB_WAY,
                MOLPAY_MALAYSIA,
                MOLPAY_THAILAND,
                MOLPAY_VIETNAM,
                OPEN_BANKING,
                SEPA,
                BACS,
                SCHEME,
                BLIK,
                WECHAT_PAY_SDK,
                PIX
        ));

        SUPPORTED_ACTION_ONLY_PAYMENT_METHODS = Collections.unmodifiableList(Arrays.asList(
                WECHAT_PAY_SDK,
                PIX
        ));

        UNSUPPORTED_PAYMENT_METHODS = Collections.unmodifiableList(Arrays.asList(
                BCMC_QR,
                AFTER_PAY,
                WECHAT_PAY_MINI_PROGRAM,
                WECHAT_PAY_QR,
                WECHAT_PAY_WEB,

                MULTIBANCO,
                OXXO,
                DOKU,
                DOKU_ALFMART,
                DOKU_PERMATA_LITE_ATM,
                DOKU_INDOMARET,
                DOKU_ATM_MANDIRI_VA,
                DOKU_SINARMAS_VA,
                DOKU_MANDIRI_VA,
                DOKU_CIMB_VA,
                DOKU_DANAMON_VA,
                DOKU_BRI_VA,
                DOKU_BNI_VA,
                DOKU_BCA_VA,
                DOKU_WALLET,

                BOLETOBANCARIO,
                BOLETOBANCARIO_BANCODOBRASIL,
                BOLETOBANCARIO_BRADESCO,
                BOLETOBANCARIO_HSBC,
                BOLETOBANCARIO_ITAU,
                BOLETOBANCARIO_SANTANDER,

                DRAGONPAY_EBANKING,
                DRAGONPAY_OTC_BANKING,
                DRAGONPAY_OTC_NON_BANKING,
                DRAGONPAY_OTC_PHILIPPINES,

                ECONTEXT_SEVEN_ELEVEN,
                ECONTEXT_ATM,
                ECONTEXT_STORES,
                ECONTEXT_ONLINE
        ));
    }

    private PaymentMethodTypes() {
        throw new NoConstructorException();
    }
}
