/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.ui.core.old.internal.ui.model.AddressParams

internal data class BoletoComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val isSubmitButtonVisible: Boolean,
    val addressParams: AddressParams,
    val isEmailVisible: Boolean,
) : ComponentParams by commonComponentParams, ButtonParams
