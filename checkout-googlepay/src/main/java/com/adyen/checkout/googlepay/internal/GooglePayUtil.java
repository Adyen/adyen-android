/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 23/04/2018.
 */

package com.adyen.checkout.googlepay.internal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.Objects;
import com.adyen.checkout.core.card.CardType;
import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.Amount;
import com.adyen.checkout.core.model.GooglePayConfiguration;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.util.AmountFormat;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class GooglePayUtil {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.US));

    @NonNull
    static Task<Boolean> getIsReadyToPayTask(
            @NonNull Context context,
            @NonNull PaymentSession paymentSession,
            @NonNull GooglePayConfiguration configuration
    ) {
        PaymentsClient paymentsClient = GooglePayUtil.buildPaymentClient(context, configuration);
        JSONObject isReadyToPayRequestJson = GooglePayUtil.getIsReadyToPayRequest(paymentSession);
        IsReadyToPayRequest isReadyToPayRequest = IsReadyToPayRequest.fromJson(isReadyToPayRequestJson.toString());
        return paymentsClient.isReadyToPay(isReadyToPayRequest);
    }

    @NonNull
    static Task<PaymentData> getPaymentDataTask(
            @NonNull Context context,
            @NonNull PaymentSession paymentSession,
            @NonNull GooglePayConfiguration configuration
    ) {
        PaymentsClient paymentsClient = GooglePayUtil.buildPaymentClient(context, configuration);
        JSONObject paymentDataRequestJson = GooglePayUtil.getPaymentDataRequest(paymentSession, configuration);
        PaymentDataRequest paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());

        return paymentsClient.loadPaymentData(paymentDataRequest);
    }

    @NonNull
    static JSONArray getAllowedCardNetworks(@NonNull PaymentSession paymentSession) {
        JSONArray allowedCardNetworks = new JSONArray();

        List<PaymentMethod> paymentMethods = paymentSession.getPaymentMethods();

        Map<CardType, String> supportedCardTypes = getSupportedCardTypes();

        for (Map.Entry<CardType, String> cardTypeEntry : supportedCardTypes.entrySet()) {
            if (PaymentMethodImpl.findByType(paymentMethods, cardTypeEntry.getKey().getTxVariant()) != null) {
                allowedCardNetworks.put(cardTypeEntry.getValue());
            }
        }

        return allowedCardNetworks;
    }

    @NonNull
    static String getPaymentToken(Intent data) throws JSONException {
        PaymentData paymentData = Objects.requireNonNull(PaymentData.getFromIntent(data), "PaymentData is null.");
        JSONObject paymentDataJson = new JSONObject(paymentData.toJson());
        JSONObject paymentMethodDataJson = paymentDataJson.getJSONObject("paymentMethodData");
        JSONObject tokenizationDataJson = paymentMethodDataJson.getJSONObject("tokenizationData");

        return tokenizationDataJson.getString("token");
    }

    @NonNull
    private static PaymentsClient buildPaymentClient(@NonNull Context context, @NonNull GooglePayConfiguration configuration) {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(configuration.getEnvironment())
                .build();

        return Wallet.getPaymentsClient(context, walletOptions);
    }

    @NonNull
    private static JSONObject getIsReadyToPayRequest(@NonNull PaymentSession paymentSession) {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest();
            isReadyToPayRequest.put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod(paymentSession)));

            return isReadyToPayRequest;
        } catch (JSONException e) {
            throw new RuntimeException("Could not create IsReadyToPayRequest JSON.");
        }
    }

    @NonNull
    private static JSONObject getPaymentDataRequest(
            @NonNull PaymentSession paymentSession,
            @NonNull GooglePayConfiguration googlePayConfiguration
    ) {
        try {
            JSONObject paymentDataRequest = getBaseRequest();
            paymentDataRequest.put("allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod(paymentSession, googlePayConfiguration)));
            paymentDataRequest.put("transactionInfo", getTransactionInfo(paymentSession.getPayment().getAmount()));
            paymentDataRequest.put("merchantInfo", getMerchantInfo());

            return paymentDataRequest;
        } catch (JSONException e) {
            throw new RuntimeException("Could not create PaymentDataRequest JSON.");
        }
    }

    @NonNull
    private static Map<CardType, String> getSupportedCardTypes() {
        HashMap<CardType, String> cardTypes = new HashMap<>();

        cardTypes.put(CardType.AMERICAN_EXPRESS, "AMEX");
        cardTypes.put(CardType.DISCOVER, "DISCOVER");
        cardTypes.put(CardType.JCB, "JCB");
        cardTypes.put(CardType.MASTERCARD, "MASTERCARD");
        cardTypes.put(CardType.VISA, "VISA");

        return cardTypes;
    }

    @NonNull
    private static JSONObject getMerchantInfo() throws JSONException {
        return new JSONObject().put("merchantName", "Example Merchant");
    }

    @NonNull
    private static JSONObject getTransactionInfo(@NonNull Amount amount) throws JSONException {
        BigDecimal bigDecimal = AmountFormat.toBigDecimal(amount);
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
        String displayAmount = DECIMAL_FORMAT.format(bigDecimal);

        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", displayAmount);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("currencyCode", amount.getCurrency());

        return transactionInfo;
    }

    @NonNull
    private static JSONObject getCardPaymentMethod(
            @NonNull PaymentSession paymentSession,
            @NonNull GooglePayConfiguration googlePayConfiguration
    ) throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod(paymentSession);
        cardPaymentMethod.put("tokenizationSpecification", getTokenizationSpecification(googlePayConfiguration));

        return cardPaymentMethod;
    }

    @NonNull
    private static JSONObject getBaseCardPaymentMethod(@NonNull PaymentSession paymentSession) throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");
        cardPaymentMethod.put(
                "parameters",
                new JSONObject()
                        .put("allowedAuthMethods", getAllowedCardAuthMethods())
                        .put("allowedCardNetworks", getAllowedCardNetworks(paymentSession)));

        return cardPaymentMethod;
    }

    @NonNull
    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray()
                .put("PAN_ONLY")
                .put("CRYPTOGRAM_3DS");
    }

    @NonNull
    private static JSONObject getTokenizationSpecification(@NonNull GooglePayConfiguration googlePayConfiguration) throws JSONException {
        JSONObject tokenizationSpecification = new JSONObject();
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
        tokenizationSpecification.put(
                "parameters",
                new JSONObject()
                        .put("gateway", googlePayConfiguration.getGateway())
                        .put("gatewayMerchantId", googlePayConfiguration.getGatewayMerchantId()));

        return tokenizationSpecification;
    }

    @NonNull
    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    private GooglePayUtil() {
        throw new IllegalStateException("No instances.");
    }
}
