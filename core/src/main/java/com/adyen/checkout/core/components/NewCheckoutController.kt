/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatDelegate
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

// TODO - rename later
interface CheckoutControllerInterface {
    suspend fun submit()
}

fun NewCheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext,
    callbacks: CheckoutCallbacks,
    applicationContext: Context,
    coroutineScope: CoroutineScope,
): NewCheckoutController {
    when (context) {
        // TODO - Move this logic to the a different location
        is CheckoutContext.Advanced -> {
            val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
                checkoutConfiguration = context.checkoutConfiguration,
                deviceLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault(),
                dropInOverrideParams = null,
                componentSessionParams = null,
                publicKey = context.publicKey,
            )

            val analyticsManager = AnalyticsManagerFactory().provide(
                componentParams = componentParamsBundle.commonComponentParams,
                applicationContext = applicationContext,
                source = AnalyticsSource.PaymentComponent("paymentMethod.type"),
                sessionId = null,
                checkoutAttemptId = context.checkoutAttemptId,
            )

            return NewCheckoutController(
                target = target,
                context = context,
                callbacks = callbacks,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                checkoutConfiguration = context.checkoutConfiguration,
                componentParamsBundle = componentParamsBundle,
            )
        }

        is CheckoutContext.Sessions -> {
            val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
                checkoutConfiguration = context.checkoutConfiguration,
                deviceLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault(),
                dropInOverrideParams = null,
                componentSessionParams = SessionParamsFactory.create(context.checkoutSession),
                publicKey = context.publicKey,
            )

            val analyticsManager = AnalyticsManagerFactory().provide(
                componentParams = componentParamsBundle.commonComponentParams,
                applicationContext = applicationContext,
                source = AnalyticsSource.PaymentComponent("paymentMethod.type"),
                sessionId = context.checkoutSession.sessionSetupResponse.id,
                checkoutAttemptId = context.checkoutAttemptId,
            )

            return NewCheckoutController(
                target = target,
                context = context,
                callbacks = callbacks,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                checkoutConfiguration = context.checkoutConfiguration,
                componentParamsBundle = componentParamsBundle,
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NewCheckoutController(
    private val target: CheckoutTarget,
    private val context: CheckoutContext,
    @Suppress("unused")
    private val callbacks: CheckoutCallbacks,
    private val coroutineScope: CoroutineScope,
    private val analyticsManager: AnalyticsManager,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val componentParamsBundle: ComponentParamsBundle,
) : CheckoutControllerInterface {

    internal val paymentComponent: PaymentComponent<*>?

    init {
        paymentComponent = when (target) {
            is CheckoutTarget.PaymentMethod -> {
                val paymentMethod = getPaymentMethodResponse()?.paymentMethods?.find { it.type == target.txVariant }

                if (paymentMethod == null) {
                    null
                } else {
                    PaymentMethodProvider.getPaymentComponent(
                        paymentMethod = paymentMethod,
                        coroutineScope = coroutineScope,
                        analyticsManager = analyticsManager,
                        checkoutConfiguration = checkoutConfiguration,
                        componentParamsBundle = componentParamsBundle,
                        checkoutCallbacks = callbacks,
                    )
                }
            }

            is CheckoutTarget.StoredPaymentMethod -> {
                val storedPaymentMethod = getPaymentMethodResponse()?.storedPaymentMethods?.find { it.id == target.id }

                if (storedPaymentMethod == null) {
                    null
                } else {
                    PaymentMethodProvider.getStoredPaymentComponent(
                        storedPaymentMethod = storedPaymentMethod,
                        coroutineScope = coroutineScope,
                        analyticsManager = analyticsManager,
                        checkoutConfiguration = checkoutConfiguration,
                        componentParamsBundle = componentParamsBundle,
                        checkoutCallbacks = callbacks,
                    )
                }
            }

            else -> null
        }
    }

    private fun getPaymentMethodResponse(): PaymentMethods? {
        return when (context) {
            is CheckoutContext.Advanced -> context.paymentMethods
            is CheckoutContext.Sessions -> context.checkoutSession.sessionSetupResponse.paymentMethods
        }
    }

    // TODO - Ensure state is valid, handle state being null, add validate function and support sessions
    override suspend fun submit() {
//        if (_state.value is CheckoutControllerState.PaymentMethod) {
//            componentStateFlow?.value?.let {
//                callbacks.beforeSubmit?.beforeSubmit(it)
//                callbacks.onSubmit?.onSubmit(it.data)
//            }
//        }
    }
}
