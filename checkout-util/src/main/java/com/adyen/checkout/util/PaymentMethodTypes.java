package com.adyen.checkout.util;

/**
 * An incomplete list of payment method types.
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 10/04/2018.
 */
public final class PaymentMethodTypes {
    /**
     * @deprecated Android Pay has been deprecated in favor of Google Pay.
     */
    @Deprecated
    public static final String ANDROID_PAY = "androidpay";

    public static final String BCMC = "bcmc";

    public static final String CARD = "card";

    public static final String CUP = "cup";

    public static final String DOKU = "doku";

    public static final String GIROPAY = "giropay";

    public static final String GOOGLE_PAY = "paywithgoogle";

    public static final String IDEAL = "ideal";

    public static final String MOLPAY_EBANKING_FPX_MY = "molpay_ebanking_fpx_MY";

    public static final String MOLPAY_EBANKING_TH = "molpay_ebanking_TH";

    public static final String PAYPAL = "paypal";

    public static final String QIWIWALLET = "qiwiwallet";

    public static final String SAMSUNG_PAY = "samsungpay";

    public static final String SEPA_DIRECT_DEBIT = "sepadirectdebit";

    public static final String WECHAT_PAY = "wechatpay";

    public static final String WECHAT_PAY_QR = "wechatpayQR";

    public static final String WECHAT_PAY_SDK = "wechatpaySDK";

    public static final String WECHAT_PAY_WEB = "wechatpayWeb";

    private PaymentMethodTypes() {
        throw new IllegalStateException("No instances.");
    }
}
