/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.ui.model.ComponentParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class GenericComponentParams(
    private val commonComponentParams: CommonComponentParams,
) : ComponentParams by commonComponentParams
