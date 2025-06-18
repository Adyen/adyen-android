/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.InputData
import com.adyen.checkout.core.old.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.old.ui.model.ExpiryDate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardInputData(
    var cardNumber: String = "",
    var expiryDate: ExpiryDate = EMPTY_DATE,
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
