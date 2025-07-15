/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.ui.core.old.internal.ui.model.AddressParams

internal data class ACHDirectDebitComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val isSubmitButtonVisible: Boolean,
    val addressParams: AddressParams,
    val isStorePaymentFieldVisible: Boolean,
) : ComponentParams by commonComponentParams, ButtonParams
