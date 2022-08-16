/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/7/2022.
 */

package com.adyen.checkout.card

import androidx.annotation.StringRes
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface CardDelegate : PaymentMethodDelegate<CardConfiguration, CardInputData, CardOutputData, CardComponentState> {

    val inputData: CardInputData

    val outputDataFlow: Flow<CardOutputData?>

    val componentStateFlow: Flow<CardComponentState?>

    val exceptionFlow: Flow<CheckoutException>

    fun initialize(coroutineScope: CoroutineScope)

    fun isDualBrandedFlow(cardOutputData: CardOutputData): Boolean

    fun isInstallmentsRequired(cardOutputData: CardOutputData): Boolean

    @StringRes fun getKcpBirthDateOrTaxNumberHint(input: String): Int

    fun requiresInput(): Boolean

    fun onCleared()
}
