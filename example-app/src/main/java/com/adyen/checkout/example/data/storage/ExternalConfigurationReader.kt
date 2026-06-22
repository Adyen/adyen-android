/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.example.data.storage

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Base64 as JavaBase64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads external SDK configuration from a Base64-encoded JSON string (passed via intent extras)
 * and writes the parsed values to [KeyValueStorage] so the existing
 * [com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider] picks them up.
 *
 * The JSON schema uses unified keys aligned with the native SDKs (e.g. `showCardholderName`).
 * All fields are optional — omitted fields leave the stored default unchanged.
 */
@Singleton
class ExternalConfigurationReader @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
) {

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    /**
     * Decodes the Base64-encoded JSON config and applies it to storage.
     *
     * @param configBase64 Base64-encoded JSON string, or null if no config was passed.
     */
    fun apply(configBase64: String?) {
        if (configBase64.isNullOrEmpty()) return

        val json = runCatching {
            String(JavaBase64.getDecoder().decode(configBase64))
        }.getOrNull() ?: return

        val config = runCatching {
            moshi.adapter(ExternalConfiguration::class.java).fromJson(json)
        }.getOrNull() ?: return

        config.card?.let { applyCardConfiguration(it) }
    }

    private fun applyCardConfiguration(card: ExternalCardConfiguration) {
        card.showCardholderName?.let { keyValueStorage.setShowCardholderName(it) }
    }
}

internal data class ExternalConfiguration(
    @Json(name = "CARD_CONFIGURATION") val card: ExternalCardConfiguration? = null,
)

internal data class ExternalCardConfiguration(
    @Json(name = "showCardholderName") val showCardholderName: Boolean? = null,
)
