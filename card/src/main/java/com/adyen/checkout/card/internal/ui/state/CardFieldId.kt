/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.form.FormFieldId

/**
 * Every element of the card form, whether or not the shopper can currently see it.
 *
 * The declaration order carries no meaning; the order the shopper sees is `canonicalCardFieldOrder`.
 */
internal enum class CardFieldId(override val isTextInput: Boolean) : FormFieldId {
    CARD_NUMBER(isTextInput = true),
    EXPIRY_DATE(isTextInput = true),
    SECURITY_CODE(isTextInput = true),
    HOLDER_NAME(isTextInput = true),
    SOCIAL_SECURITY_NUMBER(isTextInput = true),
    KCP_BIRTH_DATE_OR_TAX_NUMBER(isTextInput = true),
    KCP_CARD_PASSWORD(isTextInput = true),
    POSTAL_CODE(isTextInput = true),
    STORE_PAYMENT_METHOD(isTextInput = false),
    INSTALLMENTS(isTextInput = false),
}
