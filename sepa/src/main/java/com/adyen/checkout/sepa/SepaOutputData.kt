/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.sepa

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState

data class SepaOutputData(
    val ownerNameField: FieldState<String>,
    val ibanNumberField: FieldState<String>,
    val iban: Iban?,
    override val isValid: Boolean,
) : OutputData
