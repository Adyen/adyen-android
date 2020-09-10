/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/7/2019.
 */

package com.adyen.checkout.base.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class holding currency information.
 * @see <a href="https://docs.adyen.com/developers/currency-codes">Adyen currency codes</a>
 */
public enum CheckoutCurrency {
    AED(2),
    ALL(2),
    AMD(2),
    ANG(2),
    AOA(2),
    ARS(2),
    AUD(2),
    AWG(2),
    AZN(2),
    BAM(2),
    BBD(2),
    BDT(2),
    BGN(2),
    BHD(3),
    BMD(2),
    BND(2),
    BOB(2),
    BRL(2),
    BSD(2),
    BWP(2),
    BYN(2),
    BZD(2),
    CAD(2),
    CHF(2),
    CLP(2),
    CNY(2),
    COP(2),
    CRC(2),
    CUP(2),
    CVE(0),
    CZK(2),
    DJF(0),
    DKK(2),
    DOP(2),
    DZD(2),
    EGP(2),
    ETB(2),
    EUR(2),
    FJD(2),
    FKP(2),
    GBP(2),
    GEL(2),
    GHS(2),
    GIP(2),
    GMD(2),
    GNF(0),
    GTQ(2),
    GYD(2),
    HKD(2),
    HNL(2),
    HRK(2),
    HTG(2),
    HUF(2),
    IDR(0),
    ILS(2),
    INR(2),
    ISK(2),
    JMD(2),
    JOD(3),
    JPY(0),
    KES(2),
    KGS(2),
    KHR(2),
    KMF(0),
    KRW(0),
    KWD(3),
    KYD(2),
    KZT(2),
    LAK(2),
    LBP(2),
    LKR(2),
    LYD(3),
    MAD(2),
    MDL(2),
    MKD(2),
    MMK(2),
    MNT(2),
    MOP(2),
    MRO(1),
    MUR(2),
    MVR(2),
    MWK(2),
    MXN(2),
    MYR(2),
    MZN(2),
    NAD(2),
    NGN(2),
    NIO(2),
    NOK(2),
    NPR(2),
    NZD(2),
    OMR(3),
    PAB(2),
    PEN(2),
    PGK(2),
    PHP(2),
    PKR(2),
    PLN(2),
    PYG(0),
    QAR(2),
    RON(2),
    RSD(2),
    RUB(2),
    RWF(0),
    SAR(2),
    SBD(2),
    SCR(2),
    SEK(2),
    SGD(2),
    SHP(2),
    SLL(2),
    SOS(2),
    STD(2),
    SVC(2),
    SZL(2),
    THB(2),
    TND(3),
    TOP(2),
    TRY(2),
    TTD(2),
    TWD(2),
    TZS(2),
    UAH(2),
    UGX(0),
    USD(2),
    UYU(2),
    UZS(2),
    VEF(2),
    VND(0),
    VUV(0),
    WST(2),
    XAF(0),
    XCD(2),
    XOF(0),
    XPF(0),
    YER(2),
    ZAR(2),
    ZMW(2);

    private static final Map<String, CheckoutCurrency> CURRENCIES_HASHMAP;
    static {
        final HashMap<String, CheckoutCurrency> hashMap = new HashMap<>();
        for (CheckoutCurrency checkoutCurrency : CheckoutCurrency.values()) {
            hashMap.put(checkoutCurrency.name(), checkoutCurrency);
        }
        CURRENCIES_HASHMAP = Collections.unmodifiableMap(hashMap);
    }

    private final int mFractionDigits;

    /**
     * Check if the currency code is supported by Adyen.
     *
     * @param currency the 3 letter code of the currency.
     * @return if the currency exists and is supported by Adyen
     */
    public static boolean isSupported(@Nullable String currency) {
        return !TextUtils.isEmpty(currency) && CURRENCIES_HASHMAP.containsKey(currency);
    }

    /**
     * Find the instance of {@link CheckoutCurrency} based on the currency code.
     *
     * @param currency The currency code.
     * @return The CheckoutCurrency instance, or throws a {@link com.adyen.checkout.core.exeption.CheckoutException} if the code is not supported.
     */
    @NonNull
    public static CheckoutCurrency find(@Nullable String currency) {
        CurrencyUtils.assertCurrency(currency);
        //noinspection ConstantConditions
        return CURRENCIES_HASHMAP.get(currency);
    }

    CheckoutCurrency(int fractionDigits) {
        mFractionDigits = fractionDigits;
    }

    public int getFractionDigits() {
        return mFractionDigits;
    }
}
