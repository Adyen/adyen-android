/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

import android.content.Context
import com.adyen.checkout.cashapppay.CashAppPayComponent
import com.adyen.checkout.cashapppay.CashAppPayConfiguration
import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.cashapppay.getCashAppPayConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import java.util.Locale

internal class CashAppPayComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    @Suppress("ThrowsCount", "LongParameterList")
    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        paymentMethod: PaymentMethod,
        context: Context,
    ): CashAppPayComponentParams {
        val clientId = paymentMethod.configuration?.clientId ?: throw ComponentException(
            "Cannot launch Cash App Pay, clientId is missing in the payment method object.",
        )

        val scopeId = paymentMethod.configuration?.scopeId ?: throw ComponentException(
            "Cannot launch Cash App Pay, scopeId is missing in the payment method object.",
        )

        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )

        val cashAppPayConfiguration = checkoutConfiguration.getCashAppPayConfiguration()

        val params = mapToParamsInternal(
            commonComponentParams = commonComponentParamsMapperData.commonComponentParams,
            sessionParams = commonComponentParamsMapperData.sessionParams,
            dropInOverrideParams = dropInOverrideParams,
            cashAppPayConfiguration = cashAppPayConfiguration,
            clientId = clientId,
            scopeId = scopeId,
            context = context,
        )

        if (params.returnUrl == null) {
            throw ComponentException(
                "Cannot launch Cash App Pay, set the returnUrl in your CashAppPayConfiguration.Builder",
            )
        }

        return params
    }

    @Suppress("LongParameterList")
    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        @Suppress("UNUSED_PARAMETER") storedPaymentMethod: StoredPaymentMethod,
        context: Context,
    ): CashAppPayComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )

        val cashAppPayConfiguration = checkoutConfiguration.getCashAppPayConfiguration()

        return mapToParamsInternal(
            commonComponentParams = commonComponentParamsMapperData.commonComponentParams,
            sessionParams = commonComponentParamsMapperData.sessionParams,
            dropInOverrideParams = dropInOverrideParams,
            cashAppPayConfiguration = cashAppPayConfiguration,
            clientId = null,
            scopeId = null,
            context = context,
        )
    }

    @Suppress("LongParameterList")
    private fun mapToParamsInternal(
        commonComponentParams: CommonComponentParams,
        sessionParams: SessionParams?,
        dropInOverrideParams: DropInOverrideParams?,
        cashAppPayConfiguration: CashAppPayConfiguration?,
        clientId: String?,
        scopeId: String?,
        context: Context,
    ): CashAppPayComponentParams {
        return CashAppPayComponentParams(
            commonComponentParams = commonComponentParams,
            isSubmitButtonVisible = dropInOverrideParams?.isSubmitButtonVisible
                ?: cashAppPayConfiguration?.isSubmitButtonVisible ?: true,
            cashAppPayEnvironment = getCashAppPayEnvironment(
                commonComponentParams.environment,
                cashAppPayConfiguration,
            ),
            returnUrl = getReturnUrl(
                sessionParams,
                commonComponentParams.isCreatedByDropIn,
                cashAppPayConfiguration,
                context,
            ),
            showStorePaymentField = getShowStorePaymentField(sessionParams, cashAppPayConfiguration),
            storePaymentMethod = cashAppPayConfiguration?.storePaymentMethod ?: false,
            clientId = clientId,
            scopeId = scopeId,
        )
    }

    private fun getCashAppPayEnvironment(
        environment: Environment,
        cashAppPayConfiguration: CashAppPayConfiguration?
    ): CashAppPayEnvironment {
        return when {
            cashAppPayConfiguration?.cashAppPayEnvironment != null -> cashAppPayConfiguration.cashAppPayEnvironment
            environment == Environment.TEST -> CashAppPayEnvironment.SANDBOX
            else -> CashAppPayEnvironment.PRODUCTION
        }
    }

    private fun getReturnUrl(
        sessionParams: SessionParams?,
        isCreatedByDropIn: Boolean,
        cashAppPayConfiguration: CashAppPayConfiguration?,
        context: Context,
    ): String? {
        return sessionParams?.returnUrl
            ?: cashAppPayConfiguration?.returnUrl
            // if using drop-in and return url is not set use the return url default value
            ?: CashAppPayComponent.getReturnUrl(context).takeIf { isCreatedByDropIn }
    }

    private fun getShowStorePaymentField(
        sessionParams: SessionParams?,
        cashAppPayConfiguration: CashAppPayConfiguration?,
    ): Boolean {
        return sessionParams?.enableStoreDetails ?: cashAppPayConfiguration?.showStorePaymentField ?: true
    }
}
