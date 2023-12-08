/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2023.
 */

package com.adyen.checkout.components.core

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.CONTENTS_FILE_DESCRIPTOR
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.IgnoredOnParcel
import java.util.Locale
import kotlin.reflect.KClass

class CheckoutConfiguration(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val amount: Amount? = null,
    override val analyticsConfiguration: AnalyticsConfiguration? = null,
    @IgnoredOnParcel
    private val config: CheckoutConfiguration.() -> Unit = {},
) : Configuration {

    private val availableConfigurations = mutableMapOf<KClass<out Configuration>, Configuration>()

    init {
        this.apply(config)
    }

    // We need custom parcelization for this class to parcelize availableConfigurations.
    @SuppressLint("ParcelClassLoader")
    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(
        shopperLocale = parcel.readSerializable() as Locale,
        environment = parcel.readParcelable(Environment::class.java.classLoader)!!,
        clientKey = parcel.readString()!!,
        amount = parcel.readParcelable(Amount::class.java.classLoader),
        analyticsConfiguration = parcel.readParcelable(Amount::class.java.classLoader),
    ) {
        val size = parcel.readInt()

        repeat(size) {
            val clazz = parcel.readSerializable() as Class<Configuration>
            val config = parcel.readParcelable<Configuration>(clazz.classLoader)!!
            availableConfigurations[config::class] = config
        }
    }

    fun addConfiguration(configuration: Configuration) {
        availableConfigurations[configuration::class] = configuration
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun <T : Configuration> getConfiguration(configKlass: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return availableConfigurations[configKlass] as? T
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(shopperLocale)
        dest.writeParcelable(environment, flags)
        dest.writeString(clientKey)
        dest.writeParcelable(amount, flags)
        dest.writeParcelable(analyticsConfiguration, flags)
        dest.writeInt(availableConfigurations.size)
        availableConfigurations.forEach {
            dest.writeSerializable(it.key.java)
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
