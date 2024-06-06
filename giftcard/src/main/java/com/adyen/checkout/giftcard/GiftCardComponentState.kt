/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */
package com.adyen.checkout.giftcard

import android.os.Parcelable
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import kotlinx.parcelize.Parcelize

/**
 * Represents the state of [GiftCardComponent].
 */
@Parcelize
data class GiftCardComponentState(
    override val data: PaymentComponentData<GiftCardPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
    val lastFourDigits: String?,
    val paymentMethodName: String?,
    val giftCardAction: GiftCardAction,
) : PaymentComponentState<GiftCardPaymentMethod>, Parcelable
