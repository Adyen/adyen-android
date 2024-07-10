/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.internal.ui.model.TwintComponentParams
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultTwintDelegate(
    private val submitHandler: SubmitHandler<TwintComponentState>,
    private val analyticsManager: AnalyticsManager,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: TwintComponentParams,
) : TwintDelegate, ButtonDelegate {

    // TODO
    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<TwintComponentState> = submitHandler.submitFlow

    override fun initialize(coroutineScope: CoroutineScope) {

    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<TwintComponentState>) -> Unit
    ) {

    }

    override fun removeObserver() {

    }

    override fun onSubmit() {

    }

    override fun isConfirmationRequired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun shouldShowSubmitButton(): Boolean {
        TODO("Not yet implemented")
    }

    override fun shouldEnableSubmitButton(): Boolean {
        TODO("Not yet implemented")
    }

    internal fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getPaymentMethodType(): String =
        paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun onCleared() {
        removeObserver()
    }
}
