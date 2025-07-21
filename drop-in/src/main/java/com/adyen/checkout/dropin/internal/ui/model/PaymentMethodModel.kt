/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/11/2020.
 */

package com.adyen.checkout.dropin.internal.ui.model

import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem

internal data class PaymentMethodModel(
    val index: Int,
    val type: String,
    val name: String,
    val icon: String,
    val drawIconBorder: Boolean,
    // We need the environment to load the logo
    val environment: Environment,
    val brandList: List<LogoTextItem>
) : PaymentMethodListItem {
    override fun getViewType(): Int = PaymentMethodListItem.PAYMENT_METHOD
}
