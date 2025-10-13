/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */
package com.adyen.checkout.card.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.old.CardBrand
import com.adyen.checkout.card.old.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.InputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardInputData(
    var cardNumber: String = "",
    var expiryDate: String = "",
    var securityCode: String = "",
    var holderName: String = "",
    var socialSecurityNumber: String = "",
    var kcpBirthDateOrTaxNumber: String = "",
    var kcpCardPassword: String = "",
    var postalCode: String = "",
    var address: AddressInputModel = AddressInputModel(),
    var isStorePaymentMethodSwitchChecked: Boolean = false,
    var selectedCardBrand: CardBrand? = null,
    var installmentOption: InstallmentModel? = null,
) : InputData
