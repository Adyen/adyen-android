/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/2/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.model.FieldId

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface DelegateStateFactory<S : DelegateState, FI : FieldId> {
    fun createDefaultDelegateState(): S

    fun getFieldIds(): List<FI>
}
