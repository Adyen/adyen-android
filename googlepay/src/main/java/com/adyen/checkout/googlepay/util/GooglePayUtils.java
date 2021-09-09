/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/7/2019.
 */

package com.adyen.checkout.googlepay.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.model.payments.request.GooglePayPaymentMethod;
import com.adyen.checkout.components.util.AmountFormat;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.NoConstructorException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.googlepay.model.CardParameters;
import com.adyen.checkout.googlepay.model.GooglePayParams;
import com.adyen.checkout.googlepay.model.GooglePayPaymentMethodModel;
import com.adyen.checkout.googlepay.model.IsReadyToPayRequestModel;
import com.adyen.checkout.googlepay.model.PaymentDataRequestModel;
import com.adyen.checkout.googlepay.model.PaymentMethodTokenizationSpecification;
import com.adyen.checkout.googlepay.model.TokenizationParameters;
import com.adyen.checkout.googlepay.model.TransactionInfoModel;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public final class GooglePayUtils {
    private static final String TAG = LogUtil.getTag();

    private static final DecimalFormat GOOGLE_PAY_DECIMAL_FORMAT = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.ROOT));
    private static final int GOOGLE_PAY_DECIMAL_SCALE = 2;

    // IsReadyToPayRequestModel
    private static final int MAJOR_API_VERSION = 2;
    private static final int MINOT_API_VERSION = 0;

    //GooglePayPaymentMethodModel
    private static final String PAYMENT_TYPE_CARD = "CARD";

    // TokenizationSpecification
    private static final String PAYMENT_GATEWAY = "PAYMENT_GATEWAY";

    // TokenizationParameters
    private static final String ADYEN_GATEWAY = "adyen";

    // TransactionInfoModel


    // PaymentData result JSON keys
    private static final String PAYMENT_METHOD_DATA = "paymentMethodData";
    private static final String INFO = "info";
    private static final String CARD_NETWORK = "cardNetwork";
    private static final String TOKENIZATION_DATA = "tokenizationData";
    private static final String TOKEN = "token";

    private static final String NOT_CURRENTLY_KNOWN = "NOT_CURRENTLY_KNOWN";

    /**
     * Create a {@link com.google.android.gms.wallet.Wallet.WalletOptions} based on the component configuration.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The WalletOptions object.
     */
    @NonNull
    public static Wallet.WalletOptions createWalletOptions(@NonNull GooglePayParams params) {
        return new Wallet.WalletOptions.Builder()
                .setEnvironment(params.getGooglePayEnvironment())
                .build();
    }

    /**
     * Create a {@link IsReadyToPayRequest} based on the component configuration that can be used to verify Google Pay availability.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The IsReadyToPayRequest to start the task to verify Google Pay availability
     */
    @NonNull
    public static IsReadyToPayRequest createIsReadyToPayRequest(@NonNull GooglePayParams params) {
        final IsReadyToPayRequestModel isReadyToPayRequestModel = createIsReadyToPayRequestModel(params);
        final String requestJsonString = IsReadyToPayRequestModel.SERIALIZER.serialize(isReadyToPayRequestModel).toString();
        return IsReadyToPayRequest.fromJson(requestJsonString);
    }

    /**
     * Create a {@link PaymentDataRequest} based on the component configuration that can be used to start the Google Pay payment.
     *
     * @param params The parameters based on the Google Pay component configuration.
     * @return The PaymentDataRequest to start the Google Pay payment flow.
     */
    @NonNull
    public static PaymentDataRequest createPaymentDataRequest(@NonNull GooglePayParams params) {
        final PaymentDataRequestModel paymentDataRequestModel = createPaymentDataRequestModel(params);
        final String requestJsonString = PaymentDataRequestModel.SERIALIZER.serialize(paymentDataRequestModel).toString();
        return PaymentDataRequest.fromJson(requestJsonString);
    }

    /**
     * Find the token required by Adyen on the payments/ call for Google Pay.
     *
     * @param paymentData The PaymentData result from Google Pay.
     * @return The token string.
     * @throws CheckoutException If failed to find the token.
     */
    @NonNull
    public static String findToken(@NonNull PaymentData paymentData) throws CheckoutException {
        try {
            final JSONObject paymentDataJson = new JSONObject(paymentData.toJson());
            final JSONObject paymentMethodDataJson = paymentDataJson.getJSONObject(PAYMENT_METHOD_DATA);
            final JSONObject tokenizationDataJson = paymentMethodDataJson.getJSONObject(TOKENIZATION_DATA);
            return tokenizationDataJson.getString(TOKEN);
        } catch (JSONException e) {
            throw new CheckoutException("Failed to find Google Pay token.", e);
        }
    }

    /**
     * Create the PaymentMethod object from Google Pay based on the response from the SDK.
     *
     * @param paymentData The response from Google Pay SDK.
     * @param paymentMethodType the type of the payment method.
     * @return The object matching the data for the API call to Adyen.
     */
    @Nullable
    public static GooglePayPaymentMethod createGooglePayPaymentMethod(@Nullable PaymentData paymentData, @Nullable String paymentMethodType) {
        if (paymentData == null) {
            return null;
        }

        final GooglePayPaymentMethod paymentMethod = new GooglePayPaymentMethod();
        paymentMethod.setType(paymentMethodType);

        try {
            final JSONObject paymentDataJson = new JSONObject(paymentData.toJson());
            final JSONObject paymentMethodDataJson = paymentDataJson.getJSONObject(PAYMENT_METHOD_DATA);

            final JSONObject tokenizationDataJson = paymentMethodDataJson.getJSONObject(TOKENIZATION_DATA);
            paymentMethod.setGooglePayToken(tokenizationDataJson.getString(TOKEN));

            final JSONObject infoJson = paymentMethodDataJson.optJSONObject(INFO);
            if (infoJson != null && infoJson.has(CARD_NETWORK)) {
                paymentMethod.setGooglePayCardNetwork(infoJson.getString(CARD_NETWORK));
            }

            return paymentMethod;
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to find Google Pay token.", e);
            return null;
        }
    }

    private static IsReadyToPayRequestModel createIsReadyToPayRequestModel(@NonNull GooglePayParams params) {
        final IsReadyToPayRequestModel isReadyToPayRequestModel = new IsReadyToPayRequestModel();
        isReadyToPayRequestModel.setApiVersion(MAJOR_API_VERSION);
        isReadyToPayRequestModel.setApiVersionMinor(MINOT_API_VERSION);
        isReadyToPayRequestModel.setExistingPaymentMethodRequired(params.isExistingPaymentMethodRequired());

        final ArrayList<GooglePayPaymentMethodModel> allowedPaymentMethods = new ArrayList<>();
        allowedPaymentMethods.add(createCardPaymentMethod(params));
        isReadyToPayRequestModel.setAllowedPaymentMethods(allowedPaymentMethods);

        return isReadyToPayRequestModel;
    }

    private static PaymentDataRequestModel createPaymentDataRequestModel(@NonNull GooglePayParams params) {
        final PaymentDataRequestModel paymentDataRequestModel = new PaymentDataRequestModel();

        paymentDataRequestModel.setApiVersion(MAJOR_API_VERSION);
        paymentDataRequestModel.setApiVersionMinor(MINOT_API_VERSION);
        paymentDataRequestModel.setMerchantInfo(params.getMerchantInfo());
        paymentDataRequestModel.setTransactionInfo(createTransactionInfo(params));

        final ArrayList<GooglePayPaymentMethodModel> allowedPaymentMethods = new ArrayList<>();
        allowedPaymentMethods.add(createCardPaymentMethod(params));
        paymentDataRequestModel.setAllowedPaymentMethods(allowedPaymentMethods);

        paymentDataRequestModel.setEmailRequired(params.isEmailRequired());
        paymentDataRequestModel.setShippingAddressRequired(params.isShippingAddressRequired());
        paymentDataRequestModel.setShippingAddressParameters(params.getShippingAddressParameters());



        return paymentDataRequestModel;
    }

    private static GooglePayPaymentMethodModel createCardPaymentMethod(@NonNull GooglePayParams params) {
        final GooglePayPaymentMethodModel cardPaymentMethod = new GooglePayPaymentMethodModel();
        cardPaymentMethod.setType(PAYMENT_TYPE_CARD);
        cardPaymentMethod.setParameters(createCardParameters(params));
        cardPaymentMethod.setTokenizationSpecification(createTokenizationSpecification(params));
        return cardPaymentMethod;
    }

    private static CardParameters createCardParameters(@NonNull GooglePayParams params) {
        final CardParameters cardParameters = new CardParameters();
        cardParameters.setAllowedAuthMethods(params.getAllowedAuthMethods());
        cardParameters.setAllowedCardNetworks(params.getAllowedCardNetworks());
        cardParameters.setAllowPrepaidCards(params.isAllowPrepaidCards());
        cardParameters.setBillingAddressRequired(params.isBillingAddressRequired());
        cardParameters.setBillingAddressParameters(params.getBillingAddressParameters());
        return cardParameters;
    }

    private static PaymentMethodTokenizationSpecification createTokenizationSpecification(@NonNull GooglePayParams params) {
        final PaymentMethodTokenizationSpecification tokenizationSpecification = new PaymentMethodTokenizationSpecification();
        tokenizationSpecification.setType(PAYMENT_GATEWAY);
        tokenizationSpecification.setParameters(createGatewayParameters(params));
        return tokenizationSpecification;
    }

    private static TokenizationParameters createGatewayParameters(@NonNull GooglePayParams params) {
        final TokenizationParameters tokenizationParameters = new TokenizationParameters();
        tokenizationParameters.setGateway(ADYEN_GATEWAY);
        tokenizationParameters.setGatewayMerchantId(params.getGatewayMerchantId());
        return tokenizationParameters;
    }

    @NonNull
    private static TransactionInfoModel createTransactionInfo(@NonNull GooglePayParams params) {
        BigDecimal bigDecimal = AmountFormat.toBigDecimal(params.getAmount());
        bigDecimal = bigDecimal.setScale(GOOGLE_PAY_DECIMAL_SCALE, RoundingMode.HALF_UP);
        final String displayAmount = GOOGLE_PAY_DECIMAL_FORMAT.format(bigDecimal);

        final TransactionInfoModel transactionInfoModel = new TransactionInfoModel();
        // Google requires to not pass the price when the price status is NOT_CURRENTLY_KNOWN
        if (!params.getTotalPriceStatus().equals(NOT_CURRENTLY_KNOWN)) {
            transactionInfoModel.setTotalPrice(displayAmount);
        }
        transactionInfoModel.setCountryCode(params.getCountryCode());
        transactionInfoModel.setTotalPriceStatus(params.getTotalPriceStatus());
        transactionInfoModel.setCurrencyCode(params.getAmount().getCurrency());

        return transactionInfoModel;
    }

    private GooglePayUtils() {
        throw new NoConstructorException();
    }
}
