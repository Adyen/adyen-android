/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/5/2025.
 */

package com.adyen.checkout.core

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.CONTENTS_FILE_DESCRIPTOR
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.data.model.Amount
import com.adyen.checkout.core.internal.Configuration
import com.adyen.checkout.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.internal.util.LocaleUtil
import kotlinx.parcelize.IgnoredOnParcel
import java.util.Locale

/**
 * A generic configuration class that allows customizing the Checkout library.
 * You can use the block parameter to add drop-in or payment method specific configurations. For example:
 *
 * ```
 * val checkoutConfiguration = CheckoutConfiguration(
 *     environment,
 *     clientKey,
 *     shopperLocale, // optional
 *     amount, // not applicable with the Sessions flow
 * ) {
 *     dropIn {
 *         setEnableRemovingStoredPaymentMethods(true)
 *     }
 *     card {
 *         setHolderNameRequired(true)
 *     }
 * }
 * ```
 *
 * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
 * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
 * @param shopperLocale The [Locale] used to display information to the shopper. By default the shopper locale will
 * match the value passed to the API with the sessions flow, or the primary user locale on the device otherwise. Check
 * out the [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
 * this value.
 * @param amount The amount of the transaction. Not applicable for the sessions flow. Check out the
 * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set this
 * value.
 * @param analyticsConfiguration A configuration for the internal analytics of the library.
 * @param configurationBlock A block that allows adding drop-in or payment method specific configurations.
 */
@CheckoutConfigurationMarker
class CheckoutConfiguration(
    override val environment: Environment,
    override val clientKey: String,
    override val shopperLocale: Locale? = null,
    override val amount: Amount? = null,
    // TODO - Analytics Configuration
//    override val analyticsConfiguration: AnalyticsConfiguration? = null,
    @IgnoredOnParcel
    private val configurationBlock: CheckoutConfiguration.() -> Unit = {},
) : Configuration {

    private val availableConfigurations = mutableMapOf<String, Configuration>()

    init {
        apply(configurationBlock)
        validateContents()
    }

    @Suppress("TooGenericExceptionThrown")
    private fun validateContents() {
        shopperLocale?.let {
            if (!LocaleUtil.isValidLocale(it)) {
                // TODO - Error propagation
                throw Exception("Invalid shopper locale: $shopperLocale.")
            }
        }
    }

    // We need custom parcelization for this class to parcelize availableConfigurations.
    @SuppressLint("ParcelClassLoader")
    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    private constructor(parcel: Parcel) : this(
        // the order in which these fields are read from Parcel should match the order in which they are written to
        // Parcel in the `writeToParcel` function
        shopperLocale = parcel.readSerializable() as? Locale,
        environment = requireNotNull(parcel.readParcelable(Environment::class.java.classLoader)),
        clientKey = requireNotNull(parcel.readString()),
        amount = parcel.readParcelable(Amount::class.java.classLoader),
//        analyticsConfiguration = parcel.readParcelable(AnalyticsConfiguration::class.java.classLoader),
    ) {
        val size = parcel.readInt()

        repeat(size) {
            val key = requireNotNull(parcel.readString())
            val configClass = parcel.readSerializable() as Class<Configuration>
            val config = requireNotNull(parcel.readParcelable<Configuration>(configClass.classLoader))
            availableConfigurations[key] = config
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun addConfiguration(key: String, configuration: Configuration) {
        availableConfigurations[key] = configuration
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun addActionConfiguration(configuration: Configuration) {
        availableConfigurations[configuration::class.java.name] = configuration
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : Configuration> getConfiguration(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return availableConfigurations[key] as? T
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : Configuration> getActionConfiguration(configClass: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return availableConfigurations[configClass.name] as? T
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // the order in which these fields are written from Parcel should match the order in which they are read from
        // Parcel in `constructor(parcel: Parcel)`
        dest.writeSerializable(shopperLocale)
        dest.writeParcelable(environment, flags)
        dest.writeString(clientKey)
        dest.writeParcelable(amount, flags)
//        dest.writeParcelable(analyticsConfiguration, flags)
        dest.writeInt(availableConfigurations.size)
        availableConfigurations.forEach {
            dest.writeString(it.key)
            dest.writeSerializable(it.value::class.java)
            dest.writeParcelable(it.value, flags)
        }
    }

    override fun describeContents(): Int = CONTENTS_FILE_DESCRIPTOR

    companion object CREATOR : Parcelable.Creator<CheckoutConfiguration> {

        override fun createFromParcel(source: Parcel): CheckoutConfiguration {
            return CheckoutConfiguration(source)
        }

        override fun newArray(size: Int): Array<CheckoutConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}
