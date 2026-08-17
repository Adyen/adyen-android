/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.example.data.storage

import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Reads external SDK configuration from a Base64-encoded JSON string (passed via intent extras)
 * and keeps the parsed values in memory so [com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider]
 * can apply them as overrides on top of the persisted [KeyValueStorage] settings.
 *
 * The JSON schema uses unified keys aligned with the native SDKs (e.g. `showCardholderName`).
 * All fields are optional — omitted fields leave the corresponding override unset.
 *
 * When [configBase64] is null or empty (normal app launch without test extras), any previously
 * applied override is cleared to prevent state leaking between e2e runs.
 */
@OptIn(ExperimentalEncodingApi::class)
@Singleton
internal class ExternalConfigurationReader @Inject constructor() {

    var cardConfiguration: ExternalCardConfiguration? = null
        private set

    fun apply(configBase64: String?) {
        if (configBase64.isNullOrEmpty()) {
            cardConfiguration = null
            return
        }

        cardConfiguration = decode(configBase64)?.card
    }

    private fun decode(configBase64: String): ExternalConfiguration? {
        val json = runCatching {
            String(Base64.decode(configBase64), Charsets.UTF_8)
        }.getOrNull() ?: return null

        return runCatching {
            ExternalConfiguration.fromJson(json)
        }.getOrNull()
    }
}

internal data class ExternalConfiguration(
    val card: ExternalCardConfiguration? = null,
) {
    companion object {
        private const val KEY_CARD_CONFIGURATION = "CARD_CONFIGURATION"

        fun fromJson(json: String): ExternalConfiguration {
            val root = JSONObject(json)
            val cardJson = root.optJSONObject(KEY_CARD_CONFIGURATION)
            return ExternalConfiguration(
                card = cardJson?.let { ExternalCardConfiguration.fromJson(it) },
            )
        }
    }
}

internal data class ExternalCardConfiguration(
    val showCardholderName: Boolean? = null,
) {
    companion object {
        private const val KEY_SHOW_CARDHOLDER_NAME = "showCardholderName"

        fun fromJson(json: JSONObject): ExternalCardConfiguration {
            return ExternalCardConfiguration(
                showCardholderName = if (json.has(KEY_SHOW_CARDHOLDER_NAME)) {
                    json.getBoolean(KEY_SHOW_CARDHOLDER_NAME)
                } else {
                    null
                },
            )
        }
    }
}
