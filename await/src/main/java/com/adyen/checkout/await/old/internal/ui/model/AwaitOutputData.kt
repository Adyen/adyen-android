/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.await.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.OutputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AwaitOutputData(
    override val isValid: Boolean,
    val paymentMethodType: String?,
) : OutputData
