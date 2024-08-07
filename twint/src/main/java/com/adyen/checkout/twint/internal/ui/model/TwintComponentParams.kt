/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.ui.model

import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams

internal data class TwintComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val isSubmitButtonVisible: Boolean,
    val showStorePaymentField: Boolean,
    val storePaymentMethod: Boolean,
    val actionHandlingMethod: ActionHandlingMethod,
) : ComponentParams by commonComponentParams, ButtonParams
