package com.adyen.checkout.components.base

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.components.util.ValidationUtils
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.util.LocaleUtil
import java.util.Locale

/**
 * Base constructor with the required fields.
 *
 * @param shopperLocale The Locale of the shopper.
 * @param environment   The [Environment] to be used for network calls to Adyen.
 * @param clientKey     Your Client Key used for network calls from the SDK to Adyen.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class BaseConfigurationBuilder<
    ConfigurationT : Configuration,
    BuilderT : BaseConfigurationBuilder<ConfigurationT, BuilderT>
    >(
    protected var shopperLocale: Locale,
    protected var environment: Environment,
    protected var clientKey: String
) {

    protected var isAnalyticsEnabled: Boolean? = null
    protected var amount: Amount = Amount.EMPTY

    init {
        if (!ValidationUtils.isClientKeyValid(clientKey)) {
            throw CheckoutException("Client key is not valid.")
        }
    }

    /**
     * Constructor that provides default values.
     *
     * @param context A Context
     * @param environment   The [Environment] to be used for network calls to Adyen.
     * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
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
     * Sets if components can send analytics events.
     *
     * Default is True.
     *
     * @param isAnalyticsEnabled Is analytics should be enabled or not.
     */
    fun setAnalyticsEnabled(isAnalyticsEnabled: Boolean): BuilderT {
        this.isAnalyticsEnabled = isAnalyticsEnabled
        @Suppress("UNCHECKED_CAST")
        return this as BuilderT
    }

    /**
     * TODO docs
     */
    fun setAmount(amount: Amount): BuilderT {
        if (!CheckoutCurrency.isSupported(amount.currency)) {
            throw CheckoutException("Currency code is not valid.")
        }
        if (amount.value < 0) {
            throw CheckoutException("Value cannot be less than 0.")
        }
        this.amount = amount
        @Suppress("UNCHECKED_CAST")
        return this as BuilderT
    }

    protected abstract fun buildInternal(): ConfigurationT

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
