/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2023.
 */

package com.adyen.checkout.instant.internal.ui.model

import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams

internal data class InstantComponentParams(
    private val commonComponentParams: CommonComponentParams,
    val actionHandlingMethod: ActionHandlingMethod,
) : ComponentParams by commonComponentParams
