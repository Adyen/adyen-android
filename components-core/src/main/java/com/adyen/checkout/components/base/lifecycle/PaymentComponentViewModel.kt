/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */
package com.adyen.checkout.components.base.lifecycle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

/**
 * Base class of a PaymentComponent as a ViewModel.
 *
 * @param <ConfigurationT>  A Configuration object although optional is required to construct a Component.
 * @param <ComponentStateT> The [PaymentComponentState] this Component returns as a result.
 */
abstract class PaymentComponentViewModel<
    ConfigurationT : Configuration,
    ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
    val savedStateHandle: SavedStateHandle,
    override val configuration: ConfigurationT
) : ViewModel(), PaymentComponent<ComponentStateT, ConfigurationT>
