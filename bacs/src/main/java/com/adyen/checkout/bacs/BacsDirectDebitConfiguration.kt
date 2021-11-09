/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.*

class BacsDirectDebitConfiguration : Configuration {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BacsDirectDebitConfiguration?> = object : Parcelable.Creator<BacsDirectDebitConfiguration?> {
            override fun createFromParcel(source: Parcel?): BacsDirectDebitConfiguration? {
                if (source == null) return null
                return BacsDirectDebitConfiguration(source)
            }

            override fun newArray(size: Int): Array<BacsDirectDebitConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }

    internal constructor(builder: Builder) : super(builder.builderShopperLocale, builder.builderEnvironment, builder.builderClientKey)
    internal constructor(parcel: Parcel) : super(parcel)

    class Builder : BaseConfigurationBuilder<BacsDirectDebitConfiguration> {

        constructor(context: Context, clientKey: String) : super(context, clientKey)

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(shopperLocale, environment, clientKey)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        override fun buildInternal(): BacsDirectDebitConfiguration {
            return BacsDirectDebitConfiguration(this)
        }
    }
}
