/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.action

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.action.GenericActionConfiguration.Builder
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import java.util.Locale
import kotlin.collections.set

/**
 * This is the base configuration for the Action handling component. You need to use the [Builder] to instantiate this
 * class.
 * There you will find specific methods to add configurations for each specific ActionComponent, to be able to customize
 * their behavior.
 * If you don't specify anything, a default configuration will be used.
 */
class GenericActionConfiguration : Configuration, Parcelable {

    private val availableActionConfigs: HashMap<Class<*>, Configuration>

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<GenericActionConfiguration> {
            override fun createFromParcel(parcel: Parcel) = GenericActionConfiguration(parcel)
            override fun newArray(size: Int) = arrayOfNulls<GenericActionConfiguration>(size)
        }
    }

    constructor(
        builder: Builder
    ) : super(builder.builderShopperLocale, builder.builderEnvironment, builder.builderClientKey) {
        this.availableActionConfigs = builder.availableActionConfigs
    }

    constructor(parcel: Parcel) : super(parcel) {
        @Suppress("UNCHECKED_CAST")
        availableActionConfigs =
            parcel.readHashMap(Configuration::class.java.classLoader) as HashMap<Class<*>, Configuration>
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeMap(availableActionConfigs)
    }

    internal inline fun <reified T : Configuration> getConfigurationForAction(): T? {
        val actionClass = T::class.java
        if (availableActionConfigs.containsKey(actionClass)) {
            @Suppress("UNCHECKED_CAST")
            return availableActionConfigs[actionClass] as T
        }
        return null
    }

    /**
     * Builder for creating a [GenericActionConfiguration] where you can set specific Configurations for an action
     */
    class Builder(
        builderShopperLocale: Locale,
        builderEnvironment: Environment,
        builderClientKey: String
    ) : BaseConfigurationBuilder<GenericActionConfiguration>(
        builderShopperLocale,
        builderEnvironment,
        builderClientKey
    ) {

        val availableActionConfigs = HashMap<Class<*>, Configuration>()

        /**
         * Add configuration for 3DS2 action.
         */
        fun add3ds2ActionConfiguration(configuration: Adyen3DS2Configuration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Await action.
         */
        fun addAwaitActionConfiguration(configuration: AwaitConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for QR code action.
         */
        fun addQRCodeActionConfiguration(configuration: QRCodeConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Redirect action.
         */
        fun addRedirectActionConfiguration(configuration: RedirectConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for WeChat Pay action.
         */
        fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Voucher action.
         */
        fun addVoucherActionConfiguration(configuration: VoucherConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        override fun buildInternal(): GenericActionConfiguration {
            return GenericActionConfiguration(this)
        }
    }
}
