/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/5/2025.
 */

package com.adyen.checkout.core.components

import android.os.Parcelable
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.internal.Configuration
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * A generic configuration class that allows customizing the Checkout library.
 * You can use the block parameter to add drop-in or payment method specific configurations. For example:
 *
 * ```
 * val checkoutConfiguration = CheckoutConfiguration(
 *     environment = Environment.TEST,
 *     clientKey = clientKey,
 *     shopperLocale = Locale.US, // optional
 * ) {
 *     dropIn(hideStoredPaymentMethods = true)
 *     card(showCardholderName = true)
 * }
 * ```
 *
 * @property environment The [Environment] to be used for internal network calls from the SDK to Adyen.
 * @property clientKey Your Client Key used for internal network calls from the SDK to Adyen.
 * @property shopperLocale The [Locale] used to display information to the shopper. By default the shopper locale will
 * match the value passed to the API with the sessions flow, or the primary user locale on the device otherwise. Check
 * out the [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
 * this value.
 * @property analyticsConfiguration A configuration for the internal analytics of the library.
 * @property showSubmitButton Determines if the submit button should be visible or not. For drop-in this will be
 * ignored.
 */
@Parcelize
class CheckoutConfiguration internal constructor(
    val environment: Environment,
    val clientKey: String,
    val shopperLocale: Locale?,
    val analyticsConfiguration: AnalyticsConfiguration?,
    val showSubmitButton: Boolean?,
    private val availableConfigurations: Map<String, Configuration>,
) : Parcelable {

    /**
     * Creates a [CheckoutConfiguration].
     *
     * @param configurationBlock A block that allows adding drop-in or payment method specific configurations.
     */
    constructor(
        environment: Environment,
        clientKey: String,
        shopperLocale: Locale? = null,
        analyticsConfiguration: AnalyticsConfiguration? = null,
        showSubmitButton: Boolean? = null,
        configurationBlock: CheckoutConfigurationScope.() -> Unit = {},
    ) : this(
        environment = environment,
        clientKey = clientKey,
        shopperLocale = shopperLocale,
        analyticsConfiguration = analyticsConfiguration,
        showSubmitButton = showSubmitButton,
        availableConfigurations = CheckoutConfigurationScope().apply(configurationBlock).getConfigurations(),
    )

    internal fun getAvailableConfigurations(): Map<String, Configuration> {
        return availableConfigurations
    }
}
