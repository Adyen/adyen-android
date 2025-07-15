/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.ui.core.old.internal.ui.model.AddressOutputData
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AddressDelegate {
    val addressOutputData: AddressOutputData
    val addressOutputDataFlow: Flow<AddressOutputData>
    fun updateAddressInputData(update: AddressInputModel.() -> Unit)
}
