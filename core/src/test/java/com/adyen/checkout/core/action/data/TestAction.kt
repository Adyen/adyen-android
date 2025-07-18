/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.core.action.data

import kotlinx.parcelize.Parcelize

@Parcelize
data class TestAction(
    override val type: String? = "test",
    override val paymentData: String? = null,
    override val paymentMethodType: String? = null,
) : Action()
