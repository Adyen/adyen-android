/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/12/2021.
 */

package com.adyen.checkout.components.base

import com.adyen.checkout.components.model.payments.Amount

/**
 * Represents a configuration builder class that contains an amount.
 */
interface AmountConfigurationBuilder {
    fun setAmount(amount: Amount): AmountConfigurationBuilder
}
