/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.data.storage

import androidx.annotation.Keep

@Keep
enum class IntegrationRegion(val countryCode: String, val currency: String) {
    AU("AU", "AUD"),
    AT("AT", "EUR"),
    BE("BE", "EUR"),
    BR("BR", "BRL"),
    CA("CA", "CAD"),
    CN("CN", "CNY"),
    CZ("CZ", "CZK"),
    DK("DK", "DKK"),
    FI("FI", "EUR"),
    FR("FR", "EUR"),
    DE("DE", "EUR"),
    HK("HK", "HKD"),
    IN("IN", "INR"),
    ID("ID", "IDR"),
    IT("IT", "EUR"),
    JP("JP", "JPY"),
    KE("KE", "KES"),
    MY("MY", "MYR"),
    MX("MX", "MXN"),
    NL("NL", "EUR"),
    NO("NO", "NOK"),
    NZ("NZ", "NZD"),
    PH("PH", "PHP"),
    PL("PL", "PLN"),
    PT("PT", "EUR"),
    RU("RU", "RUB"),
    SG("SG", "SGD"),
    KR("KR", "KRW"),
    ES("ES", "EUR"),
    SE("SE", "SEK"),
    CH("CH", "CHF"),
    TH("TH", "THB"),
    AE("AE", "AED"),
    GB("GB", "GBP"),
    US("US", "USD"),
    VN("VN", "VND"),
    ZA("ZA", "ZAR"),
}
