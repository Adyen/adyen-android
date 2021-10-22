package com.adyen.checkout.card

enum class InstallmentOption(val type: String?) {
    ONE_TIME(null),
    REGULAR("regular"),
    REVOLVING("revolving")
}
