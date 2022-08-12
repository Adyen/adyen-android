/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */
package com.adyen.checkout.components.util

import java.util.Collections

/**
 * Helper class with a list of all the currently supported Payment Methods on Components and Drop-In.
 */
object PaymentMethodTypes {

    // Placeholder value if the type is not found.
    const val UNKNOWN = "unknown"

    // Type of the payment method as received by the paymentMethods/ API
    const val IDEAL = "ideal"
    const val MOLPAY_MALAYSIA = "molpay_ebanking_fpx_MY"
    const val MOLPAY_THAILAND = "molpay_ebanking_TH"
    const val MOLPAY_VIETNAM = "molpay_ebanking_VN"
    const val DOTPAY = "dotpay"
    const val EPS = "eps"
    const val ENTERCASH = "entercash"
    const val OPEN_BANKING = "openbanking_UK"
    const val SCHEME = "scheme"
    const val GOOGLE_PAY = "googlepay"
    const val GOOGLE_PAY_LEGACY = "paywithgoogle"
    const val SEPA = "sepadirectdebit"
    const val BACS = "directdebit_GB"
    const val BCMC = "bcmc"
    const val MB_WAY = "mbway"
    const val BLIK = "blik"
    const val GIFTCARD = "giftcard"
    const val ONLINE_BANKING_PL = "onlineBanking_PL"

    // Payment methods that do not need a payment component, but only an action component
    const val WECHAT_PAY_SDK = "wechatpaySDK"
    const val PIX = "pix"

    // Voucher payment methods that are not yet supported
    const val MULTIBANCO = "multibanco"
    const val OXXO = "oxxo"
    const val DOKU = "doku"
    const val DOKU_ALFMART = "doku_alfamart"
    const val DOKU_PERMATA_LITE_ATM = "doku_permata_lite_atm"
    const val DOKU_INDOMARET = "doku_indomaret"
    const val DOKU_ATM_MANDIRI_VA = "doku_atm_mandiri_va"
    const val DOKU_SINARMAS_VA = "doku_sinarmas_va"
    const val DOKU_MANDIRI_VA = "doku_mandiri_va"
    const val DOKU_CIMB_VA = "doku_cimb_va"
    const val DOKU_DANAMON_VA = "doku_danamon_va"
    const val DOKU_BRI_VA = "doku_bri_va"
    const val DOKU_BNI_VA = "doku_bni_va"
    const val DOKU_BCA_VA = "doku_bca_va"
    const val DOKU_WALLET = "doku_wallet"
    const val BOLETOBANCARIO = "boletobancario"
    const val BOLETOBANCARIO_BANCODOBRASIL = "boletobancario_bancodobrasil"
    const val BOLETOBANCARIO_BRADESCO = "boletobancario_bradesco"
    const val BOLETOBANCARIO_HSBC = "boletobancario_hsbc"
    const val BOLETOBANCARIO_ITAU = "boletobancario_itau"
    const val BOLETOBANCARIO_SANTANDER = "boletobancario_santander"
    const val DRAGONPAY_EBANKING = "dragonpay_ebanking"
    const val DRAGONPAY_OTC_BANKING = "dragonpay_otc_banking"
    const val DRAGONPAY_OTC_NON_BANKING = "dragonpay_otc_non_banking"
    const val DRAGONPAY_OTC_PHILIPPINES = "dragonpay_otc_philippines"
    const val ECONTEXT_SEVEN_ELEVEN = "econtext_seven_eleven"
    const val ECONTEXT_ATM = "econtext_atm"
    const val ECONTEXT_STORES = "econtext_stores"
    const val ECONTEXT_ONLINE = "econtext_online"

    // Payment methods that might be interpreted as redirect, but are actually not supported
    const val BCMC_QR = "bcmc_mobile_QR"
    const val AFTER_PAY = "afterpay_default"
    const val WECHAT_PAY_MINI_PROGRAM = "wechatpayMiniProgram"
    const val WECHAT_PAY_QR = "wechatpayQR"
    const val WECHAT_PAY_WEB = "wechatpayWeb"

    // List of all payment method types.
    val SUPPORTED_PAYMENT_METHODS: List<String> = Collections.unmodifiableList(
        listOf(
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
            PIX,
            ONLINE_BANKING_PL
        )
    )
    val SUPPORTED_ACTION_ONLY_PAYMENT_METHODS: List<String> = Collections.unmodifiableList(
        listOf(
            WECHAT_PAY_SDK,
            PIX
        )
    )
    val UNSUPPORTED_PAYMENT_METHODS: List<String> = Collections.unmodifiableList(
        listOf(
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
        )
    )
}
