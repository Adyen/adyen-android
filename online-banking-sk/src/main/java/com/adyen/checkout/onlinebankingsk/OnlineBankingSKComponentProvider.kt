/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.onlinebankingsk

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingSKPaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.onlinebankingcore.DefaultOnlineBankingDelegate
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.PdfOpener

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OnlineBankingSKComponentProvider(
    overrideComponentParams: ComponentParams? = null
) : PaymentComponentProvider<OnlineBankingComponent<OnlineBankingSKPaymentMethod>, OnlineBankingSKConfiguration> {

    private val componentParamsMapper = GenericComponentParamsMapper(overrideComponentParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: OnlineBankingSKConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?,
    ): OnlineBankingComponent<OnlineBankingSKPaymentMethod> {
        assertSupported(paymentMethod)

        val genericFactory: ViewModelProvider.Factory =
            viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
                val componentParams = componentParamsMapper.mapToParams(configuration)
                val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
                val analyticsService = AnalyticsService(httpClient)
                val analyticsRepository = DefaultAnalyticsRepository(
                    packageName = application.packageName,
                    locale = componentParams.shopperLocale,
                    source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
                    analyticsService = analyticsService,
                    analyticsMapper = AnalyticsMapper(),
                )
                val delegate =
                    DefaultOnlineBankingDelegate(
                        observerRepository = PaymentObserverRepository(),
                        pdfOpener = PdfOpener(),
                        paymentMethod = paymentMethod,
                        componentParams = componentParams,
                        analyticsRepository = analyticsRepository,
                        termsAndConditionsUrl = OnlineBankingSKComponent.TERMS_CONDITIONS_URL
                    ) { OnlineBankingSKPaymentMethod() }

                OnlineBankingSKComponent(
                    savedStateHandle,
                    delegate,
                )
            }
        return ViewModelProvider(viewModelStoreOwner, genericFactory)[key, OnlineBankingSKComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return OnlineBankingSKComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
