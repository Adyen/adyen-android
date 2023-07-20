package com.adyen.checkout.components.core.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.components.core.internal.util.validate
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LocaleUtil
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class BaseConfigurationBuilder<
    ConfigurationT : Configuration,
    BuilderT : BaseConfigurationBuilder<ConfigurationT, BuilderT>
    >
/**
 * Initialize a configuration builder with the required fields.
 *
 * @param shopperLocale The [Locale] of the shopper.
 * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
 * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
 */
constructor(
    protected var shopperLocale: Locale,
    protected var environment: Environment,
    protected var clientKey: String
) {

    protected var analyticsConfiguration: AnalyticsConfiguration? = null
    protected var amount: Amount = Amount.EMPTY

    init {
        if (!ValidationUtils.isClientKeyValid(clientKey)) {
            throw CheckoutException("Client key is not valid.")
        }
    }

    /**
     * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
     *
     * @param context A Context
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
     */
    constructor(
        context: Context,
        environment: Environment,
        clientKey: String
    ) : this(
        LocaleUtil.getLocale(context),
        environment,
        clientKey
    )

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
     * Default is [Amount.EMPTY].
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
        if (!ValidationUtils.doesClientKeyMatchEnvironment(clientKey, environment)) {
            throw CheckoutException("Client key does not match the environment.")
        }

        if (!LocaleUtil.isValidLocale(shopperLocale)) {
            throw CheckoutException("Invalid shopper locale: $shopperLocale.")
        }

        return buildInternal()
    }
}
