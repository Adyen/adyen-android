/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 24/1/2023.
 */

package com.adyen.checkout.ach

import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionComponentProvider
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.analytics.AnalyticsMapper
import com.adyen.checkout.components.analytics.AnalyticsSource
import com.adyen.checkout.components.analytics.DefaultAnalyticsRepository
import com.adyen.checkout.components.api.AddressService
import com.adyen.checkout.components.api.AnalyticsService
import com.adyen.checkout.components.api.PublicKeyService
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.DefaultAddressRepository
import com.adyen.checkout.components.repository.DefaultPublicKeyRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.cse.DefaultGenericEncrypter

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AchComponentProvider(
    overrideComponentParams: ComponentParams? = null,
) : PaymentComponentProvider<AchComponent, AchConfiguration> {

    private val componentParamsMapper = AchComponentParamsMapper(overrideComponentParams = overrideComponentParams)

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        paymentMethod: PaymentMethod,
        configuration: AchConfiguration,
        application: Application,
        defaultArgs: Bundle?,
        key: String?
    ): AchComponent {
        assertSupported(paymentMethod)
        val componentParams = componentParamsMapper.mapToParams(configuration)
        val httpClient = HttpClientFactory.getHttpClient(componentParams.environment)
        val publicKeyService = PublicKeyService(httpClient)
        val publicKeyRepository = DefaultPublicKeyRepository(publicKeyService)
        val analyticsService = AnalyticsService(httpClient)
        val addressService = AddressService(httpClient)
        val addressRepository = DefaultAddressRepository(addressService)
        val genericEncrypter = DefaultGenericEncrypter()
        val analyticsRepository = DefaultAnalyticsRepository(
            packageName = application.packageName,
            locale = componentParams.shopperLocale,
            source = AnalyticsSource.PaymentComponent(componentParams.isCreatedByDropIn, paymentMethod),
            analyticsService = analyticsService,
            analyticsMapper = AnalyticsMapper(),
        )

        val achFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val achDelegate = DefaultAchDelegate(
                observerRepository = PaymentObserverRepository(),
                paymentMethod = paymentMethod,
                analyticsRepository = analyticsRepository,
                publicKeyRepository = publicKeyRepository,
                addressRepository = addressRepository,
                submitHandler = SubmitHandler(),
                genericEncrypter = genericEncrypter,
                componentParams = componentParams,
            )

            val genericActionDelegate = GenericActionComponentProvider(componentParams).getDelegate(
                configuration = configuration.genericActionConfiguration,
                savedStateHandle = savedStateHandle,
                application = application,
            )

            AchComponent(
                achDelegate = achDelegate,
                genericActionDelegate = genericActionDelegate,
                actionHandlingComponent = DefaultActionHandlingComponent(genericActionDelegate, achDelegate),
            )
        }
        return ViewModelProvider(viewModelStoreOwner, achFactory)[key, AchComponent::class.java]
    }

    private fun assertSupported(paymentMethod: PaymentMethod) {
        if (!isPaymentMethodSupported(paymentMethod)) {
            throw ComponentException("Unsupported payment method ${paymentMethod.type}")
        }
    }

    override fun isPaymentMethodSupported(paymentMethod: PaymentMethod): Boolean {
        return AchComponent.PAYMENT_METHOD_TYPES.contains(paymentMethod.type)
    }
}
