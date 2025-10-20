/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentStateFactory<C : ComponentState> {

    fun createDefaultComponentState(): C
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultComponentStateFactory : ComponentStateFactory<DefaultComponentState> {

    override fun createDefaultComponentState() = DefaultComponentState()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultComponentState : ComponentState
