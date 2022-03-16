/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/6/2019.
 */
package com.adyen.checkout.components.analytics

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.webkit.URLUtil
import com.adyen.checkout.components.BuildConfig
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale

class AnalyticEvent : Parcelable {
    enum class Flavor {
        DROPIN, COMPONENT
    }

    private val payloadVersion = CURRENT_PAYLOAD_VERSION
    private val version = BuildConfig.CHECKOUT_VERSION
    private val flavor: String? // e.g: 'dropin', 'component'
    private val component: String? // e.g: dropin, paymentType
    private val locale: String? // e.g: en_US
    private val platform = ANDROID_PLATFORM
    private val referer: String? // e.g: package name
    private val deviceBrand = Build.BRAND
    private val deviceModel = Build.MODEL
    private val systemVersion = Build.VERSION.SDK_INT.toString()

    internal constructor(input: Parcel) {
        flavor = input.readString()
        component = input.readString()
        locale = input.readString()
        referer = input.readString()
    }

    private constructor(packageName: String, flavor: String, components: String, locale: String) {
        referer = packageName
        this.locale = locale
        this.flavor = flavor
        component = components
    }

    /**
     * Puts the event in the form of GET parameters in front of the provided base URL.
     *
     * @param baseUrl A base URL of the endpoint to send the events to.
     * @return The full URL to be called.
     */
    @Throws(MalformedURLException::class)
    fun toUrl(baseUrl: String): URL {
        if (!URLUtil.isValidUrl(baseUrl)) {
            throw MalformedURLException("Invalid URL format - $baseUrl")
        }
        val baseUri = Uri.parse(baseUrl)
        val finalUri = Uri.Builder()
            .scheme(baseUri.scheme)
            .authority(baseUri.authority)
            .path(baseUri.path)
            .appendQueryParameter(PAYLOAD_VERSION_KEY, payloadVersion)
            .appendQueryParameter(VERSION_KEY, version)
            .appendQueryParameter(FLAVOR_KEY, flavor)
            .appendQueryParameter(COMPONENT_KEY, component)
            .appendQueryParameter(LOCALE_KEY, locale)
            .appendQueryParameter(PLATFORM_KEY, platform)
            .appendQueryParameter(REFERER_KEY, referer)
            .appendQueryParameter(DEVICE_BRAND_KEY, deviceBrand)
            .appendQueryParameter(DEVICE_MODEL_KEY, deviceModel)
            .appendQueryParameter(SYSTEM_VERSION_KEY, systemVersion)
            .build()
        return URL(finalUri.toString())
    }

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(flavor)
        dest.writeString(component)
        dest.writeString(locale)
        dest.writeString(referer)
    }

    companion object {
        private const val DROPIN_FLAVOR = "dropin"
        private const val COMPONENT_FLAVOR = "components"
        private const val CURRENT_PAYLOAD_VERSION = "1"
        private const val ANDROID_PLATFORM = "android"
        private const val PAYLOAD_VERSION_KEY = "payload_version"
        private const val VERSION_KEY = "version"
        private const val FLAVOR_KEY = "flavor"
        private const val COMPONENT_KEY = "component"
        private const val LOCALE_KEY = "locale"
        private const val PLATFORM_KEY = "platform"
        private const val REFERER_KEY = "referer"
        private const val DEVICE_BRAND_KEY = "device_brand"
        private const val DEVICE_MODEL_KEY = "device_model"
        private const val SYSTEM_VERSION_KEY = "system_version"

        @JvmField
        val CREATOR: Parcelable.Creator<AnalyticEvent> = object : Parcelable.Creator<AnalyticEvent> {
            override fun createFromParcel(`in`: Parcel): AnalyticEvent {
                return AnalyticEvent(`in`)
            }

            override fun newArray(size: Int): Array<AnalyticEvent?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * Create an AnalyticEvent representing a state of the usage of the components.
         *
         * @param context A context to get the package name.
         * @param flavor One of the available flavors os integration.
         * @param components The component that was openend.
         * @param locale The user locale being used.
         * @return A new instance of an AnalyticEvent
         */
        @JvmStatic
        fun create(context: Context, flavor: Flavor, components: String, locale: Locale): AnalyticEvent {
            val flavorName = when (flavor) {
                Flavor.DROPIN -> DROPIN_FLAVOR
                Flavor.COMPONENT -> COMPONENT_FLAVOR
            }
            return AnalyticEvent(context.packageName, flavorName, components, locale.toString())
        }
    }
}
