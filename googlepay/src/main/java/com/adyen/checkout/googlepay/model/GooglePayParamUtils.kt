package com.adyen.checkout.googlepay.model

import com.adyen.checkout.googlepay.util.AllowedCardNetworks

internal object GooglePayParamUtils {

    fun mapBrandToGooglePayNetwork(brand: String): String? {
        return when {
            brand == "mc" -> AllowedCardNetworks.MASTERCARD
            AllowedCardNetworks.allAllowedCardNetworks.contains(brand.uppercase()) -> brand.uppercase()
            else -> null
        }
    }
}
