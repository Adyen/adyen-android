/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2023.
 */
package com.adyen.checkout.components.core

/**
 * Helper class with a list of all the currently supported Payment Methods on Components and Drop-In.
 */
object PaymentMethodTypes {

    // Placeholder value if the type is not found.
    const val UNKNOWN = "unknown"

    // Type of the payment method as received by the paymentMethods/ API
    const val ACH = "ach"
    const val BACS = "directdebit_GB"
    const val BCMC = "bcmc"
    const val BLIK = "blik"
    const val BOLETOBANCARIO = "boletobancario"
    const val BOLETOBANCARIO_BANCODOBRASIL = "boletobancario_bancodobrasil"
    const val BOLETOBANCARIO_BRADESCO = "boletobancario_bradesco"
    const val BOLETOBANCARIO_HSBC = "boletobancario_hsbc"
    const val BOLETOBANCARIO_ITAU = "boletobancario_itau"
    const val BOLETOBANCARIO_SANTANDER = "boletobancario_santander"
    const val BOLETO_PRIMEIRO_PAY = "primeiropay_boleto"
    const val CASH_APP_PAY = "cashapp"
    const val DOTPAY = "dotpay"
    const val ENTERCASH = "entercash"
    const val EPS = "eps"
    const val GIFTCARD = "giftcard"
    const val GOOGLE_PAY = "googlepay"
    const val GOOGLE_PAY_LEGACY = "paywithgoogle"
    const val IDEAL = "ideal"
    const val MB_WAY = "mbway"
    const val MEAL_VOUCHER_FR_GROUPEUP = "mealVoucher_FR_groupeup"
    const val MEAL_VOUCHER_FR_NATIXIS = "mealVoucher_FR_natixis"
    const val MEAL_VOUCHER_FR_SODEXO = "mealVoucher_FR_sodexo"
    const val MEAL_VOUCHER_FR = "mealVoucher_FR"
    const val MOLPAY_MALAYSIA = "molpay_ebanking_fpx_MY"
    const val MOLPAY_THAILAND = "molpay_ebanking_TH"
    const val MOLPAY_VIETNAM = "molpay_ebanking_VN"
    const val ONLINE_BANKING_CZ = "onlineBanking_CZ"
    const val ONLINE_BANKING_PL = "onlineBanking_PL"
    const val ONLINE_BANKING_SK = "onlineBanking_SK"
    const val OPEN_BANKING = "openbanking_UK"
    const val PAY_BY_BANK = "paybybank"
    const val PAY_BY_BANK_US = "paybybank_AIS_DD"
    const val SCHEME = "scheme"
    const val SEPA = "sepadirectdebit"
    const val TWINT = "twint"
    const val UPI = "upi"
    const val UPI_INTENT = "upi_intent"
    const val UPI_COLLECT = "upi_collect"
    const val UPI_QR = "upi_qr"

    // Payment methods that do not need a payment component, but only an action component
    const val DUIT_NOW = "duitnow"
    const val PAY_NOW = "paynow"
    const val PIX = "pix"
    const val PROMPT_PAY = "promptpay"
    const val WECHAT_PAY_SDK = "wechatpaySDK"
    const val MULTIBANCO = "multibanco"

    // Voucher payment methods that are not yet supported
    const val DOKU = "doku"
    const val DOKU_ALFMART = "doku_alfamart"
    const val DOKU_ATM_MANDIRI_VA = "doku_atm_mandiri_va"
    const val DOKU_BCA_VA = "doku_bca_va"
    const val DOKU_BNI_VA = "doku_bni_va"
    const val DOKU_BRI_VA = "doku_bri_va"
    const val DOKU_CIMB_VA = "doku_cimb_va"
    const val DOKU_DANAMON_VA = "doku_danamon_va"
    const val DOKU_INDOMARET = "doku_indomaret"
    const val DOKU_MANDIRI_VA = "doku_mandiri_va"
    const val DOKU_PERMATA_LITE_ATM = "doku_permata_lite_atm"
    const val DOKU_SINARMAS_VA = "doku_sinarmas_va"
    const val DOKU_WALLET = "doku_wallet"
    const val DRAGONPAY_EBANKING = "dragonpay_ebanking"
    const val DRAGONPAY_OTC_BANKING = "dragonpay_otc_banking"
    const val DRAGONPAY_OTC_NON_BANKING = "dragonpay_otc_non_banking"
    const val DRAGONPAY_OTC_PHILIPPINES = "dragonpay_otc_philippines"
    const val ECONTEXT_ATM = "econtext_atm"
    const val ECONTEXT_ONLINE = "econtext_online"
    const val ECONTEXT_SEVEN_ELEVEN = "econtext_seven_eleven"
    const val ECONTEXT_STORES = "econtext_stores"
    const val OXXO = "oxxo"

    // Payment methods that might be interpreted as redirect, but are actually not supported
    const val AFTER_PAY = "afterpay_default"
    const val BCMC_QR = "bcmc_mobile_QR"
    const val WECHAT_PAY_MINI_PROGRAM = "wechatpayMiniProgram"
    const val WECHAT_PAY_QR = "wechatpayQR"
    const val WECHAT_PAY_WEB = "wechatpayWeb"

    // List of all payment method types.
    val SUPPORTED_PAYMENT_METHODS: List<String> = listOf(
        ACH,
        BACS,
        BCMC,
        BLIK,
        BOLETOBANCARIO,
        BOLETOBANCARIO_BANCODOBRASIL,
        BOLETOBANCARIO_BRADESCO,
        BOLETOBANCARIO_HSBC,
        BOLETOBANCARIO_ITAU,
        BOLETOBANCARIO_SANTANDER,
        BOLETO_PRIMEIRO_PAY,
        CASH_APP_PAY,
        DOTPAY,
        DUIT_NOW,
        ECONTEXT_ATM,
        ECONTEXT_ONLINE,
        ECONTEXT_SEVEN_ELEVEN,
        ECONTEXT_STORES,
        ENTERCASH,
        EPS,
        GIFTCARD,
        GOOGLE_PAY,
        GOOGLE_PAY_LEGACY,
        IDEAL,
        MB_WAY,
        MEAL_VOUCHER_FR_GROUPEUP,
        MEAL_VOUCHER_FR_NATIXIS,
        MEAL_VOUCHER_FR_SODEXO,
        MOLPAY_MALAYSIA,
        MOLPAY_THAILAND,
        MOLPAY_VIETNAM,
        MULTIBANCO,
        ONLINE_BANKING_CZ,
        ONLINE_BANKING_PL,
        ONLINE_BANKING_SK,
        OPEN_BANKING,
        PAY_BY_BANK,
        PAY_BY_BANK_US,
        PAY_NOW,
        PIX,
        PROMPT_PAY,
        SCHEME,
        SEPA,
        TWINT,
        UPI,
        UPI_COLLECT,
        UPI_INTENT,
        UPI_QR,
        WECHAT_PAY_SDK,
    )

    // Payment methods that do not need a payment component, but only an action component
    @Suppress("unused")
    @Deprecated(
        """
        This list is no longer relevant nor maintained. To check which payment methods are supported by the 
        InstantPayComponent use InstantPayComponent.PROVIDER.isPaymentMethodSupported.""",
    )
    val SUPPORTED_ACTION_ONLY_PAYMENT_METHODS: List<String> = listOf(
        DUIT_NOW,
        PAY_NOW,
        PIX,
        PROMPT_PAY,
        TWINT,
        WECHAT_PAY_SDK,
        MULTIBANCO,
    )

    // Payment methods that are explicitly unsupported
    val UNSUPPORTED_PAYMENT_METHODS: List<String> = listOf(
        AFTER_PAY,
        BCMC_QR,
        DOKU,
        DOKU_ALFMART,
        DOKU_ATM_MANDIRI_VA,
        DOKU_BCA_VA,
        DOKU_BNI_VA,
        DOKU_BRI_VA,
        DOKU_CIMB_VA,
        DOKU_DANAMON_VA,
        DOKU_INDOMARET,
        DOKU_MANDIRI_VA,
        DOKU_PERMATA_LITE_ATM,
        DOKU_SINARMAS_VA,
        DOKU_WALLET,
        DRAGONPAY_EBANKING,
        DRAGONPAY_OTC_BANKING,
        DRAGONPAY_OTC_NON_BANKING,
        DRAGONPAY_OTC_PHILIPPINES,
        OXXO,
        WECHAT_PAY_MINI_PROGRAM,
        WECHAT_PAY_QR,
        WECHAT_PAY_WEB,
    )
}
