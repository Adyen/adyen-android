/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2022.
 */

package com.adyen.checkout.action

import android.app.Application
import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitComponent
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.AwaitAction
import com.adyen.checkout.components.model.payments.response.BaseThreeds2Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.qrcode.QRCodeComponent
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherComponent
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionComponent
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ActionDelegateProvider {

    fun get(
        action: Action,
        configuration: GenericActionConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): ActionDelegate<Action> {
        val delegate = when (action) {
            is AwaitAction -> {
                AwaitComponent.PROVIDER.getDelegate(getConfiguration(configuration), savedStateHandle, application)
            }
            is QrCodeAction -> {
                QRCodeComponent.PROVIDER.getDelegate(getConfiguration(configuration), savedStateHandle, application)
            }
            is RedirectAction -> {
                RedirectComponent.PROVIDER.getDelegate(getConfiguration(configuration), savedStateHandle, application)
            }
            is BaseThreeds2Action -> {
                Adyen3DS2Component.PROVIDER.getDelegate(getConfiguration(configuration), savedStateHandle, application)
            }
            is VoucherAction -> {
                VoucherComponent.PROVIDER.getDelegate(getConfiguration(configuration), savedStateHandle, application)
            }
            is SdkAction<*> -> {
                WeChatPayActionComponent.PROVIDER.getDelegate(
                    getConfiguration(configuration),
                    savedStateHandle,
                    application
                )
            }
            else -> null
        }

        @Suppress("UNCHECKED_CAST")
        return (delegate as? ActionDelegate<Action>)
            ?: throw CheckoutException("Can't find delegate for action: ${action.type}")
    }

    private inline fun <reified T : Configuration> getConfiguration(
        configuration: GenericActionConfiguration
    ): T {
        return configuration.getConfigurationForAction() ?: getDefaultConfiguration(configuration)
    }

    inline fun <reified T : Configuration> getDefaultConfiguration(
        configuration: Configuration
    ): T {
        val shopperLocale = configuration.shopperLocale
        val environment = configuration.environment
        val clientKey = configuration.clientKey

        val builder: BaseConfigurationBuilder<out Configuration> = when (T::class) {
            AwaitConfiguration::class -> AwaitConfiguration.Builder(shopperLocale, environment, clientKey)
            RedirectConfiguration::class -> RedirectConfiguration.Builder(shopperLocale, environment, clientKey)
            QRCodeConfiguration::class -> QRCodeConfiguration.Builder(shopperLocale, environment, clientKey)
            Adyen3DS2Configuration::class -> Adyen3DS2Configuration.Builder(shopperLocale, environment, clientKey)
            WeChatPayActionConfiguration::class -> WeChatPayActionConfiguration.Builder(
                shopperLocale,
                environment,
                clientKey
            )
            VoucherConfiguration::class -> VoucherConfiguration.Builder(shopperLocale, environment, clientKey)
            else -> throw CheckoutException("Unable to find component configuration for class - ${T::class}")
        }

        @Suppress("UNCHECKED_CAST")
        return builder.build() as T
    }
}
