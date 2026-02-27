package com.adyen.checkout.components.core.internal

import android.content.Context
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.components.core.internal.util.validate
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.util.LocaleUtil
import java.util.Locale

@Suppress(
    "ktlint:standard:discouraged-comment-location",
    "ktlint:standard:type-parameter-list-spacing",
    "ktlint:standard:kdoc",
)
abstract class BaseConfigurationBuilder<
    ConfigurationT : Configuration,
    BuilderT : BaseConfigurationBuilder<ConfigurationT, BuilderT>
    >
/**
 * Initialize a configuration builder with the required fields and a shopper locale.
 *
 * @param shopperLocale The [Locale] of the shopper.
 * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
 * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
 */
constructor(
    protected var shopperLocale: Locale?,
    protected var environment: Environment,
    protected var clientKey: String
) {

    protected var analyticsConfiguration: AnalyticsConfiguration? = null
    protected var amount: Amount? = null

    init {
        if (!ValidationUtils.isClientKeyValid(clientKey, environment)) {
            throw CheckoutException("Client key is not valid.")
        }
    }

    /**
     * Initialize a configuration builder with the required fields.
     *
     * The shopper locale will match the value passed to the API with the sessions flow, or the primary user locale
     * on the device otherwise. Check out the
     * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
     * this value.
     *
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
     */
    constructor(
        environment: Environment,
        clientKey: String
    ) : this(
        shopperLocale = null,
        environment = environment,
        clientKey = clientKey,
    )

    /**
     * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
     *
     * @param context A Context
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
     */
    @Deprecated("You can omit the context parameter")
    constructor(
        @Suppress("unused")
        context: Context,
        environment: Environment,
        clientKey: String
    ) : this(
        null,
        environment,
        clientKey,
    )

    /**
     * Allows setting the preferred locale of the shopper.
     *
     * @param shopperLocale The [Locale] of the shopper.
     */
    fun setShopperLocale(shopperLocale: Locale): BuilderT {
        this.shopperLocale = shopperLocale
        @Suppress("UNCHECKED_CAST")
        return this as BuilderT
    }

    /**
     * Allows configuring the internal analytics of the library.
     *
     * @param analyticsConfiguration the analytics configuration.
     */
    fun setAnalyticsConfiguration(analyticsConfiguration: AnalyticsConfiguration): BuilderT {
        this.analyticsConfiguration = analyticsConfiguration
        @Suppress("UNCHECKED_CAST")
        return this as BuilderT
    }

    /**
     * Sets the amount of the transaction.
     *
     * Default is null.
     *
     * Not applicable for the sessions flow. Check out the
     * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
     * this value.
     *
     * @param amount Amount of the transaction.
     */
    open fun setAmount(amount: Amount): BuilderT {
        amount.validate()
        this.amount = amount
        @Suppress("UNCHECKED_CAST")
        return this as BuilderT
    }

    protected abstract fun buildInternal(): ConfigurationT

    /**
     * Build a configuration from the builder parameters.
     */
    fun build(): ConfigurationT {
        shopperLocale?.let {
            if (!LocaleUtil.isValidLocale(it)) {
                throw CheckoutException("Invalid shopper locale: $shopperLocale.")
            }
        }

        return buildInternal()
    }
}
