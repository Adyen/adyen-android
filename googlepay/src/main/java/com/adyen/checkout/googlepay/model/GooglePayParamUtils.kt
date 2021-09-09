package com.adyen.checkout.googlepay.model

import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.googlepay.util.AllowedCardNetworks

internal object GooglePayParamUtils {

    fun mapTxVariantToGooglePayCode(txVariant: String): String {
        return when {
            txVariant == "mc" -> AllowedCardNetworks.MASTERCARD
            AllowedCardNetworks.getAllAllowedCardNetworks().contains(txVariant.uppercase()) -> txVariant.uppercase()
            else -> throw CheckoutException("txVariant $txVariant is not an allowed card network.")
        }
    }
}
