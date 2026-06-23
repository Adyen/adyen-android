/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.example.data.storage

import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads external SDK configuration from a Base64-encoded JSON string (passed via intent extras)
 * and writes the parsed values to [KeyValueStorage] so the existing
 * [com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider] picks them up.
 *
 * The JSON schema uses unified keys aligned with the native SDKs (e.g. `showCardholderName`).
 * All fields are optional — omitted fields leave the stored default unchanged.
 *
 * When [configBase64] is null or empty (normal app launch without test extras), the persisted
 * configuration is reset to defaults to prevent test pollution from previous e2e runs.
 */
@Singleton
class ExternalConfigurationReader @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
) {

    /**
     * Decodes the Base64-encoded JSON config and applies it to storage.
     *
     * @param configBase64 Base64-encoded JSON string, or null if no config was passed.
     */
    fun apply(configBase64: String?) {
        if (configBase64.isNullOrEmpty()) {
            // Reset to defaults to prevent test pollution from previous e2e runs
            keyValueStorage.setShowCardholderName(SettingsDefaults.SHOW_CARDHOLDER_NAME)
            return
        }

        decodeAndApply(configBase64)
    }

    private fun decodeAndApply(configBase64: String) {
        val json = runCatching {
            String(android.util.Base64.decode(configBase64, android.util.Base64.DEFAULT), Charsets.UTF_8)
        }.getOrNull() ?: return

        val config = runCatching {
            ExternalConfiguration.fromJson(json)
        }.getOrNull() ?: return

        config.card?.let { applyCardConfiguration(it) }
    }

    private fun applyCardConfiguration(card: ExternalCardConfiguration) {
        card.showCardholderName?.let { keyValueStorage.setShowCardholderName(it) }
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
