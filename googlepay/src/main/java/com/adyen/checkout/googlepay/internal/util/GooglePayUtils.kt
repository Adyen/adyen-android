/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/7/2019.
 */
package com.adyen.checkout.googlepay.internal.util

import com.adyen.checkout.components.core.internal.util.AmountFormat
import com.adyen.checkout.components.core.internal.util.CheckoutPlatform
import com.adyen.checkout.components.core.internal.util.CheckoutPlatformParams
import com.adyen.checkout.components.core.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.core.old.internal.util.runCompileOnly
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.SoftwareInfo
import com.adyen.checkout.googlepay.internal.data.model.CardParameters
import com.adyen.checkout.googlepay.internal.data.model.GooglePayPaymentMethodModel
import com.adyen.checkout.googlepay.internal.data.model.IsReadyToPayRequestModel
import com.adyen.checkout.googlepay.internal.data.model.PaymentDataRequestModel
import com.adyen.checkout.googlepay.internal.data.model.PaymentMethodTokenizationSpecification
import com.adyen.checkout.googlepay.internal.data.model.TokenizationParameters
import com.adyen.checkout.googlepay.internal.data.model.TransactionInfoModel
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.threeds2.ThreeDS2Service
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet.WalletOptions
import org.json.JSONException
import org.json.JSONObject
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Suppress("TooManyFunctions")
internal object GooglePayUtils {

    private val GOOGLE_PAY_DECIMAL_FORMAT = DecimalFormat("0.##", DecimalFormatSymbols(Locale.ROOT))
    private const val GOOGLE_PAY_DECIMAL_SCALE = 2

    // IsReadyToPayRequestModel
    private const val MAJOR_API_VERSION = 2
    private const val MINOT_API_VERSION = 0

    // GooglePayPaymentMethodModel
    private const val PAYMENT_TYPE_CARD = "CARD"

    // TokenizationSpecification
    private const val PAYMENT_GATEWAY = "PAYMENT_GATEWAY"

    // TokenizationParameters
    private const val ADYEN_GATEWAY = "adyen"

    // TransactionInfoModel
    // PaymentData result JSON keys
    private const val PAYMENT_METHOD_DATA = "paymentMethodData"
    private const val INFO = "info"
    private const val CARD_NETWORK = "cardNetwork"
    private const val TOKENIZATION_DATA = "tokenizationData"
    private const val TOKEN = "token"
    private const val NOT_CURRENTLY_KNOWN = "NOT_CURRENTLY_KNOWN"

    /**
     * Create a [com.google.android.gms.wallet.Wallet.WalletOptions] based on the component configuration.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The WalletOptions object.
     */
    fun createWalletOptions(params: GooglePayComponentParams): WalletOptions {
        return WalletOptions.Builder()
            .setEnvironment(params.googlePayEnvironment)
            .build()
    }

    /**
     * Create a [IsReadyToPayRequest] based on the component configuration that can be used to verify Google Pay
     * availability.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The IsReadyToPayRequest to start the task to verify Google Pay availability
     */
    fun createIsReadyToPayRequest(params: GooglePayComponentParams): IsReadyToPayRequest {
        val isReadyToPayRequestModel = createIsReadyToPayRequestModel(params)
        val requestJsonString = IsReadyToPayRequestModel.SERIALIZER.serialize(isReadyToPayRequestModel).toString()
        return IsReadyToPayRequest.fromJson(requestJsonString)
    }

    /**
     * Create a [PaymentDataRequest] based on the component configuration that can be used to start the Google Pay
     * payment.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The PaymentDataRequest to start the Google Pay payment flow.
     */
    fun createPaymentDataRequest(params: GooglePayComponentParams): PaymentDataRequest {
        val paymentDataRequestModel = createPaymentDataRequestModel(params)
        val requestJsonString = PaymentDataRequestModel.SERIALIZER.serialize(paymentDataRequestModel).toString()
        return PaymentDataRequest.fromJson(requestJsonString)
    }

    /**
     * Find the token required by Adyen on the payments/ call for Google Pay.
     *
     * @param paymentData The PaymentData result from Google Pay.
     * @return The token string.
     * @throws CheckoutException If failed to find the token.
     */
    @Throws(CheckoutException::class)
    fun findToken(paymentData: PaymentData): String {
        return try {
            val paymentDataJson = JSONObject(paymentData.toJson())
            val paymentMethodDataJson = paymentDataJson.getJSONObject(PAYMENT_METHOD_DATA)
            val tokenizationDataJson = paymentMethodDataJson.getJSONObject(TOKENIZATION_DATA)
            tokenizationDataJson.getString(TOKEN)
        } catch (e: JSONException) {
            throw CheckoutException("Failed to find Google Pay token.", e)
        }
    }

    /**
     * Create the PaymentMethod object from Google Pay based on the response from the SDK.
     *
     * @param paymentData The response from Google Pay SDK.
     * @param paymentMethodType the type of the payment method.
     * @return The object matching the data for the API call to Adyen.
     */
    fun createGooglePayPaymentMethod(
        paymentData: PaymentData?,
        paymentMethodType: String?,
        checkoutAttemptId: String?
    ): GooglePayPaymentMethod? {
        if (paymentData == null) {
            return null
        }
        return GooglePayPaymentMethod(
            type = paymentMethodType,
            checkoutAttemptId = checkoutAttemptId,
        ).apply {
            try {
                val paymentDataJson = JSONObject(paymentData.toJson())
                val paymentMethodDataJson = paymentDataJson.getJSONObject(PAYMENT_METHOD_DATA)
                val tokenizationDataJson = paymentMethodDataJson.getJSONObject(TOKENIZATION_DATA)
                googlePayToken = tokenizationDataJson.getString(TOKEN)
                val infoJson = paymentMethodDataJson.optJSONObject(INFO)
                if (infoJson != null && !infoJson.isNull(CARD_NETWORK)) {
                    googlePayCardNetwork = infoJson.getString(CARD_NETWORK)
                }
            } catch (e: JSONException) {
                adyenLog(AdyenLogLevel.ERROR, e) { "Failed to find Google Pay token." }
            }

            threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }
        }
    }

    private fun createIsReadyToPayRequestModel(params: GooglePayComponentParams): IsReadyToPayRequestModel {
        return IsReadyToPayRequestModel(
            apiVersion = MAJOR_API_VERSION,
            apiVersionMinor = MINOT_API_VERSION,
            isExistingPaymentMethodRequired = params.isExistingPaymentMethodRequired,
            allowedPaymentMethods = getAllowedPaymentMethods(params),
        )
    }

    private fun createPaymentDataRequestModel(params: GooglePayComponentParams): PaymentDataRequestModel {
        return PaymentDataRequestModel(
            apiVersion = MAJOR_API_VERSION,
            apiVersionMinor = MINOT_API_VERSION,
            merchantInfo = params.merchantInfo.addSoftwareInfo(params),
            transactionInfo = createTransactionInfo(params),
            allowedPaymentMethods = getAllowedPaymentMethods(params),
            isEmailRequired = params.isEmailRequired,
            isShippingAddressRequired = params.isShippingAddressRequired,
            shippingAddressParameters = params.shippingAddressParameters,
        )
    }

    private fun MerchantInfo?.addSoftwareInfo(params: GooglePayComponentParams): MerchantInfo {
        val integrationType = if (params.isCreatedByDropIn) {
            IntegrationType.DROP_IN
        } else {
            IntegrationType.COMPONENTS
        }
        val platform = CheckoutPlatformParams.platform.toGooglePayPlatform()
        val softwareInfo = SoftwareInfo(
            id = "${platform.value}/${integrationType.value}",
            version = CheckoutPlatformParams.version,
        )
        return this?.copy(softwareInfo = softwareInfo) ?: MerchantInfo(softwareInfo = softwareInfo)
    }

    private fun CheckoutPlatform.toGooglePayPlatform(): GooglePayPlatform = when (this) {
        CheckoutPlatform.ANDROID -> GooglePayPlatform.ANDROID
        CheckoutPlatform.FLUTTER -> GooglePayPlatform.FLUTTER
        CheckoutPlatform.REACT_NATIVE -> GooglePayPlatform.REACT_NATIVE
    }

    internal fun getAllowedPaymentMethods(params: GooglePayComponentParams): List<GooglePayPaymentMethodModel> {
        return listOf(createCardPaymentMethod(params))
    }

    private fun createCardPaymentMethod(params: GooglePayComponentParams): GooglePayPaymentMethodModel {
        return GooglePayPaymentMethodModel(
            type = PAYMENT_TYPE_CARD,
            parameters = createCardParameters(params),
            tokenizationSpecification = createTokenizationSpecification(params),
        )
    }

    private fun createCardParameters(params: GooglePayComponentParams): CardParameters {
        return CardParameters(
            allowedAuthMethods = params.allowedAuthMethods,
            allowedCardNetworks = params.allowedCardNetworks,
            isAllowPrepaidCards = params.isAllowPrepaidCards,
            isAllowCreditCards = params.isAllowCreditCards,
            isAssuranceDetailsRequired = params.isAssuranceDetailsRequired,
            isBillingAddressRequired = params.isBillingAddressRequired,
            billingAddressParameters = params.billingAddressParameters,
        )
    }

    private fun createTokenizationSpecification(
        params: GooglePayComponentParams
    ): PaymentMethodTokenizationSpecification {
        return PaymentMethodTokenizationSpecification(
            type = PAYMENT_GATEWAY,
            parameters = createGatewayParameters(params),
        )
    }

    private fun createGatewayParameters(params: GooglePayComponentParams): TokenizationParameters {
        return TokenizationParameters(
            gateway = ADYEN_GATEWAY,
            gatewayMerchantId = params.gatewayMerchantId,
        )
    }

    private fun createTransactionInfo(params: GooglePayComponentParams): TransactionInfoModel {
        return TransactionInfoModel(
            countryCode = params.countryCode,
            totalPriceStatus = params.totalPriceStatus,
            currencyCode = params.amount.currency,
            checkoutOption = params.checkoutOption,
        ).apply {
            // Google requires to not pass the price when the price status is NOT_CURRENTLY_KNOWN
            if (params.totalPriceStatus == NOT_CURRENTLY_KNOWN) return@apply
            val bigDecimalAmount = AmountFormat.toBigDecimal(params.amount)
                .setScale(GOOGLE_PAY_DECIMAL_SCALE, RoundingMode.HALF_UP)
            val displayAmount = GOOGLE_PAY_DECIMAL_FORMAT.format(bigDecimalAmount)
            totalPrice = displayAmount
        }
    }

    private enum class IntegrationType(val value: String) {
        DROP_IN("adyen-dropin"),
        COMPONENTS("adyen-components"),
    }

    private enum class GooglePayPlatform(val value: String) {
        ANDROID("android"),
        FLUTTER("flutter"),
        REACT_NATIVE("react-native"),
    }
}
