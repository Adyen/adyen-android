/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2023.
 */

package com.adyen.checkout.twint

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.twint.internal.ui.TwintDelegate

class TwintComponent internal constructor(
    private val twintDelegate: TwintDelegate,
) : ViewModel(),
    PaymentComponent {

    override val delegate: ComponentDelegate get() = twintDelegate

    init {
        twintDelegate.initialize(viewModelScope)
    }

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<TwintComponentState>) -> Unit
    ) {
        twintDelegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    internal fun removeObserver() {
        twintDelegate.removeObserver()
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        adyenLog(AdyenLogLevel.WARN) { "Interaction with TwintComponent can't be blocked" }
    }

    override fun onCleared() {
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        super.onCleared()
        twintDelegate.onCleared()
    }
}
