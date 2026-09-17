/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo

/**
 * Runs after the reducer and the validator, so it can read the errors the validator just set.
 *
 * Must not have side effects. The state update it runs in can be executed more than once.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentStatePostProcessor<C : ComponentState, I : ComponentStateIntent> {

    fun processInitialState(state: C): C

    fun process(state: C, intent: I): C
}

internal class NoPostProcessing<C : ComponentState, I : ComponentStateIntent> :
    ComponentStatePostProcessor<C, I> {

    override fun processInitialState(state: C) = state

    override fun process(state: C, intent: I) = state
}
