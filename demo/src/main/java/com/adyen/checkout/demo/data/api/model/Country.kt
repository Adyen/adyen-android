/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/2/2024.
 */

package com.adyen.checkout.demo.data.api.model

enum class Country(val title: String, val currencyCode: String, val emoji: String) {
    AU("Australia", "AUD", "🇦🇺"),
    CA("Canada", "CAD", "🇨🇦"),
    DK("Denmark", "DKK", "🇩🇰"),
    NL("Netherlands", "EUR", "🇳🇱"),
    FR("France", "EUR", "🇫🇷"),
    PT("Portugal", "EUR", "🇵🇹"),
    DE("Germany", "EUR", "🇩🇪"),
    GB("United Kingdom", "GBP", "🇬🇧"),
    HK("Hong Kong", "HKD", "🇭🇰"),
    NO("Norway", "NOK", "🇳🇴"),
    NZ("New Zealand", "NZD", "🇳🇿"),
    PL("Poland", "PLN", "🇵🇱"),
    SE("Sweden", "SEK", "🇸🇪"),
    US("United States of America", "USD", "🇺🇸"),
    JP("Japan", "JPY", "🇯🇵"),
    ZA("South Africa", "ZAR", "🇿🇦"),
}
