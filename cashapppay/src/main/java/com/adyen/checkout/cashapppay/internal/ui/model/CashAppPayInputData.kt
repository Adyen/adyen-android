/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class CashAppPayInputData(
    var isStorePaymentSelected: Boolean = false,
    var authorizationData: CashAppPayAuthorizationData? = null,
) : InputData
