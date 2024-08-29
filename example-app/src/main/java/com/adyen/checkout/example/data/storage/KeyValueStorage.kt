/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.data.storage

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.AMOUNT
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.ANALYTICS_LEVEL
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.CARD_ADDRESS_FORM_MODE
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.CARD_INSTALLMENT_OPTIONS_MODE
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.CARD_INSTALLMENT_SHOW_AMOUNT
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.CURRENCY
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.INSTANT_PAYMENT_METHOD_TYPE
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.MERCHANT_ACCOUNT
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.REMOVE_STORED_PAYMENT_METHOD
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.SHOPPER_COUNTRY
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.SHOPPER_EMAIL
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.SHOPPER_LOCALE
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.SHOPPER_REFERENCE
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.SPLIT_CARD_FUNDING_SOURCES
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.THREEDS_MODE
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry.USE_SESSIONS

@Suppress("TooManyFunctions")
interface KeyValueStorage {
    fun getShopperReference(): String
    fun setShopperReference(shopperReference: String?)
    fun getAmount(): Amount
    fun setAmount(amount: Long?)
    fun setCurrency(currency: String?)
    fun getCountry(): String
    fun setCountry(country: String?)
    fun getShopperLocale(): String?
    fun setShopperLocale(shopperLocale: String?)
    fun getThreeDSMode(): ThreeDSMode
    fun setThreeDSMode(threeDSMode: ThreeDSMode)
    fun getShopperEmail(): String?
    fun setShopperEmail(shopperEmail: String?)
    fun getMerchantAccount(): String
    fun setMerchantAccount(merchantAccount: String?)
    fun isSplitCardFundingSources(): Boolean
    fun setSplitCardFundingSources(isSplitCardFundingSources: Boolean)
    fun getCardAddressMode(): CardAddressMode
    fun setCardAddressMode(cardAddressMode: CardAddressMode)
    fun isRemoveStoredPaymentMethodEnabled(): Boolean
    fun setRemoveStoredPaymentMethodEnabled(isRemoveStoredPaymentMethodEnabled: Boolean)
    fun getInstantPaymentMethodType(): String
    fun setInstantPaymentMethodType(instantPaymentMethodType: String?)
    fun getInstallmentOptionsMode(): CardInstallmentOptionsMode
    fun setInstallmentOptionsMode(cardInstallmentOptionsMode: CardInstallmentOptionsMode)
    fun isInstallmentAmountShown(): Boolean
    fun setInstallmentAmountShown(isInstallmentAmountShown: Boolean)
    fun useSessions(): Boolean
    fun setUseSessions(useSessions: Boolean)
    fun getAnalyticsLevel(): AnalyticsLevel
    fun setAnalyticsLevel(analyticsLevel: AnalyticsLevel)
}

@Suppress("TooManyFunctions")
internal class DefaultKeyValueStorage(
    private val sharedPreferencesManager: SharedPreferencesManager
) : KeyValueStorage {

    override fun getShopperReference(): String {
        return sharedPreferencesManager.getString(SHOPPER_REFERENCE)
    }

    override fun setShopperReference(shopperReference: String?) {
        sharedPreferencesManager.putString(SHOPPER_REFERENCE, shopperReference)
    }

    override fun getAmount(): Amount {
        return Amount(
            value = sharedPreferencesManager.getLong(AMOUNT),
            currency = sharedPreferencesManager.getString(CURRENCY),
        )
    }

    override fun setAmount(amount: Long?) {
        sharedPreferencesManager.putLong(AMOUNT, amount)
    }

    override fun setCurrency(currency: String?) {
        sharedPreferencesManager.putString(CURRENCY, currency)
    }

    override fun getCountry(): String {
        return sharedPreferencesManager.getString(SHOPPER_COUNTRY)
    }

    override fun setCountry(country: String?) {
        sharedPreferencesManager.putString(SHOPPER_COUNTRY, country)
    }

    override fun getShopperLocale(): String? {
        return sharedPreferencesManager.getStringNullable(SHOPPER_LOCALE)
    }

    override fun setShopperLocale(shopperLocale: String?) {
        sharedPreferencesManager.putString(SHOPPER_LOCALE, shopperLocale)
    }

    override fun getThreeDSMode(): ThreeDSMode {
        return sharedPreferencesManager.getEnum(THREEDS_MODE)
    }

    override fun setThreeDSMode(threeDSMode: ThreeDSMode) {
        sharedPreferencesManager.putEnum(THREEDS_MODE, threeDSMode)
    }

    override fun getShopperEmail(): String? {
        return sharedPreferencesManager.getStringNullable(SHOPPER_EMAIL)
    }

    override fun setShopperEmail(shopperEmail: String?) {
        sharedPreferencesManager.putString(SHOPPER_EMAIL, shopperEmail)
    }

    override fun getMerchantAccount(): String {
        return sharedPreferencesManager.getString(MERCHANT_ACCOUNT)
    }

    override fun setMerchantAccount(merchantAccount: String?) {
        sharedPreferencesManager.putString(MERCHANT_ACCOUNT, merchantAccount)
    }

    override fun isSplitCardFundingSources(): Boolean {
        return sharedPreferencesManager.getBoolean(SPLIT_CARD_FUNDING_SOURCES)
    }

    override fun setSplitCardFundingSources(isSplitCardFundingSources: Boolean) {
        sharedPreferencesManager.putBoolean(SPLIT_CARD_FUNDING_SOURCES, isSplitCardFundingSources)
    }

    override fun getCardAddressMode(): CardAddressMode {
        return sharedPreferencesManager.getEnum(CARD_ADDRESS_FORM_MODE)
    }

    override fun setCardAddressMode(cardAddressMode: CardAddressMode) {
        sharedPreferencesManager.putEnum(CARD_ADDRESS_FORM_MODE, cardAddressMode)
    }

    override fun isRemoveStoredPaymentMethodEnabled(): Boolean {
        return sharedPreferencesManager.getBoolean(REMOVE_STORED_PAYMENT_METHOD)
    }

    override fun setRemoveStoredPaymentMethodEnabled(isRemoveStoredPaymentMethodEnabled: Boolean) {
        sharedPreferencesManager.putBoolean(REMOVE_STORED_PAYMENT_METHOD, isRemoveStoredPaymentMethodEnabled)
    }

    override fun getInstantPaymentMethodType(): String {
        return sharedPreferencesManager.getString(INSTANT_PAYMENT_METHOD_TYPE)
    }

    override fun setInstantPaymentMethodType(instantPaymentMethodType: String?) {
        sharedPreferencesManager.putString(INSTANT_PAYMENT_METHOD_TYPE, instantPaymentMethodType)
    }

    override fun getInstallmentOptionsMode(): CardInstallmentOptionsMode {
        return sharedPreferencesManager.getEnum(CARD_INSTALLMENT_OPTIONS_MODE)
    }

    override fun setInstallmentOptionsMode(cardInstallmentOptionsMode: CardInstallmentOptionsMode) {
        sharedPreferencesManager.putEnum(CARD_INSTALLMENT_OPTIONS_MODE, cardInstallmentOptionsMode)
    }

    override fun isInstallmentAmountShown(): Boolean {
        return sharedPreferencesManager.getBoolean(CARD_INSTALLMENT_SHOW_AMOUNT)
    }

    override fun setInstallmentAmountShown(isInstallmentAmountShown: Boolean) {
        sharedPreferencesManager.putBoolean(CARD_INSTALLMENT_SHOW_AMOUNT, isInstallmentAmountShown)
    }

    override fun useSessions(): Boolean {
        return sharedPreferencesManager.getBoolean(USE_SESSIONS)
    }

    override fun setUseSessions(useSessions: Boolean) {
        sharedPreferencesManager.putBoolean(USE_SESSIONS, useSessions)
    }

    override fun getAnalyticsLevel(): AnalyticsLevel {
        return sharedPreferencesManager.getEnum(ANALYTICS_LEVEL)
    }

    override fun setAnalyticsLevel(analyticsLevel: AnalyticsLevel) {
        sharedPreferencesManager.putEnum(ANALYTICS_LEVEL, analyticsLevel)
    }
}
