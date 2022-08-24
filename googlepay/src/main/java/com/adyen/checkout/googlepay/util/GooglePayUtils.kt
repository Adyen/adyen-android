/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/7/2019.
 */
package com.adyen.checkout.googlepay.util

import com.adyen.checkout.components.model.payments.request.GooglePayPaymentMethod
import com.adyen.checkout.components.util.AmountFormat.toBigDecimal
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger.e
import com.adyen.checkout.googlepay.model.CardParameters
import com.adyen.checkout.googlepay.model.GooglePayParams
import com.adyen.checkout.googlepay.model.GooglePayPaymentMethodModel
import com.adyen.checkout.googlepay.model.IsReadyToPayRequestModel
import com.adyen.checkout.googlepay.model.PaymentDataRequestModel
import com.adyen.checkout.googlepay.model.PaymentMethodTokenizationSpecification
import com.adyen.checkout.googlepay.model.TokenizationParameters
import com.adyen.checkout.googlepay.model.TransactionInfoModel
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
object GooglePayUtils {

    private val TAG = LogUtil.getTag()
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
    fun createWalletOptions(params: GooglePayParams): WalletOptions {
        return WalletOptions.Builder()
            .setEnvironment(params.googlePayEnvironment)
            .build()
    }

    /**
     * Create a [IsReadyToPayRequest] based on the component configuration that can be used to verify Google Pay availability.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The IsReadyToPayRequest to start the task to verify Google Pay availability
     */
    fun createIsReadyToPayRequest(params: GooglePayParams): IsReadyToPayRequest {
        val isReadyToPayRequestModel = createIsReadyToPayRequestModel(params)
        val requestJsonString = IsReadyToPayRequestModel.SERIALIZER.serialize(isReadyToPayRequestModel).toString()
        return IsReadyToPayRequest.fromJson(requestJsonString)
    }

    /**
     * Create a [PaymentDataRequest] based on the component configuration that can be used to start the Google Pay payment.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The PaymentDataRequest to start the Google Pay payment flow.
     */
    fun createPaymentDataRequest(params: GooglePayParams): PaymentDataRequest {
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
    fun createGooglePayPaymentMethod(paymentData: PaymentData?, paymentMethodType: String?): GooglePayPaymentMethod? {
        if (paymentData == null) {
            return null
        }
        val paymentMethod = GooglePayPaymentMethod()
        paymentMethod.type = paymentMethodType
        return try {
            val paymentDataJson = JSONObject(paymentData.toJson())
            val paymentMethodDataJson = paymentDataJson.getJSONObject(PAYMENT_METHOD_DATA)
            val tokenizationDataJson = paymentMethodDataJson.getJSONObject(TOKENIZATION_DATA)
            paymentMethod.googlePayToken = tokenizationDataJson.getString(TOKEN)
            val infoJson = paymentMethodDataJson.optJSONObject(INFO)
            if (infoJson != null && infoJson.has(CARD_NETWORK)) {
                paymentMethod.googlePayCardNetwork = infoJson.getString(CARD_NETWORK)
            }
            paymentMethod
        } catch (e: JSONException) {
            e(TAG, "Failed to find Google Pay token.", e)
            null
        }
    }

    private fun createIsReadyToPayRequestModel(params: GooglePayParams): IsReadyToPayRequestModel {
        val isReadyToPayRequestModel = IsReadyToPayRequestModel()
        isReadyToPayRequestModel.apiVersion = MAJOR_API_VERSION
        isReadyToPayRequestModel.apiVersionMinor = MINOT_API_VERSION
        isReadyToPayRequestModel.isExistingPaymentMethodRequired = params.isExistingPaymentMethodRequired
        val allowedPaymentMethods = ArrayList<GooglePayPaymentMethodModel>()
        allowedPaymentMethods.add(createCardPaymentMethod(params))
        isReadyToPayRequestModel.allowedPaymentMethods = allowedPaymentMethods
        return isReadyToPayRequestModel
    }

    private fun createPaymentDataRequestModel(params: GooglePayParams): PaymentDataRequestModel {
        val paymentDataRequestModel = PaymentDataRequestModel()
        paymentDataRequestModel.apiVersion = MAJOR_API_VERSION
        paymentDataRequestModel.apiVersionMinor = MINOT_API_VERSION
        paymentDataRequestModel.merchantInfo = params.merchantInfo
        paymentDataRequestModel.transactionInfo = createTransactionInfo(params)
        val allowedPaymentMethods = ArrayList<GooglePayPaymentMethodModel>()
        allowedPaymentMethods.add(createCardPaymentMethod(params))
        paymentDataRequestModel.allowedPaymentMethods = allowedPaymentMethods
        paymentDataRequestModel.isEmailRequired = params.isEmailRequired
        paymentDataRequestModel.isShippingAddressRequired = params.isShippingAddressRequired
        paymentDataRequestModel.shippingAddressParameters = params.shippingAddressParameters
        return paymentDataRequestModel
    }

    private fun createCardPaymentMethod(params: GooglePayParams): GooglePayPaymentMethodModel {
        val cardPaymentMethod = GooglePayPaymentMethodModel()
        cardPaymentMethod.type = PAYMENT_TYPE_CARD
        cardPaymentMethod.parameters = createCardParameters(params)
        cardPaymentMethod.tokenizationSpecification = createTokenizationSpecification(params)
        return cardPaymentMethod
    }

    private fun createCardParameters(params: GooglePayParams): CardParameters {
        val cardParameters = CardParameters()
        cardParameters.allowedAuthMethods = params.allowedAuthMethods
        cardParameters.allowedCardNetworks = params.allowedCardNetworks
        cardParameters.isAllowPrepaidCards = params.isAllowPrepaidCards
        cardParameters.isBillingAddressRequired = params.isBillingAddressRequired
        cardParameters.billingAddressParameters = params.billingAddressParameters
        return cardParameters
    }

    private fun createTokenizationSpecification(params: GooglePayParams): PaymentMethodTokenizationSpecification {
        val tokenizationSpecification = PaymentMethodTokenizationSpecification()
        tokenizationSpecification.type = PAYMENT_GATEWAY
        tokenizationSpecification.parameters = createGatewayParameters(params)
        return tokenizationSpecification
    }

    private fun createGatewayParameters(params: GooglePayParams): TokenizationParameters {
        val tokenizationParameters = TokenizationParameters()
        tokenizationParameters.gateway = ADYEN_GATEWAY
        tokenizationParameters.gatewayMerchantId = params.gatewayMerchantId
        return tokenizationParameters
    }

    private fun createTransactionInfo(params: GooglePayParams): TransactionInfoModel {
        var bigDecimal = toBigDecimal(params.amount)
        bigDecimal = bigDecimal.setScale(GOOGLE_PAY_DECIMAL_SCALE, RoundingMode.HALF_UP)
        val displayAmount = GOOGLE_PAY_DECIMAL_FORMAT.format(bigDecimal)
        val transactionInfoModel = TransactionInfoModel()
        // Google requires to not pass the price when the price status is NOT_CURRENTLY_KNOWN
        if (params.totalPriceStatus != NOT_CURRENTLY_KNOWN) {
            transactionInfoModel.totalPrice = displayAmount
        }
        transactionInfoModel.countryCode = params.countryCode
        transactionInfoModel.totalPriceStatus = params.totalPriceStatus
        transactionInfoModel.currencyCode = params.amount.currency
        return transactionInfoModel
    }
}
