/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class CardFieldId {
    CARD_NUMBER,
    SELECTED_CARD_INDEX,
    CARD_SECURITY_CODE,
    CARD_EXPIRY_DATE,
    CARD_HOLDER_NAME,
    SOCIAL_SECURITY_NUMBER,
    KCP_BIRTH_DATE_OR_TAX_NUMBER,
    KCP_CARD_PASSWORD,
    ADDRESS_POSTAL_CODE,
//    ADDRESS_LOOKUP,
    // TODO: Do these need to be separated?
//    BIRTH_DATE_OR_TAX_NUMBER,
//    CARD_PASSWORD,
//    INSTALLMENTS,
//    STORE_PAYMENT_SWITCH,
}
