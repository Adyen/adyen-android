package com.adyen.core.models;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for parsing raw response for Payment request.
 */

public class PaymentResponse {

    private static final String TAG = PaymentResponse.class.getSimpleName();

    private String generationTime;
    private String initiationUrl;
    private String paymentData;
    private String logoBaseUrl;
    private String origin;
    private String publicKeyToken;
    private String countryCode;
    private String reference;
    private String sessionValidity;

    private String publicKey;
    private String shopperReference;

    private Amount amount;

    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private List<PaymentMethod> preferredPaymentMethods = new ArrayList<>();

    private PaymentResponse() {
        // default constructor is hidden
    }

    public PaymentResponse(@NonNull final byte[] response) throws JSONException {
        JSONObject responseJSON = getJSONResponse(response);

        //Required fields. Should fail if one of them is missing
        generationTime = responseJSON.getString("generationtime");
        initiationUrl = responseJSON.getString("initiationUrl");
        paymentData = responseJSON.getString("paymentData");
        logoBaseUrl = responseJSON.getString("logoBaseUrl");
        origin = responseJSON.getString("origin");
        publicKeyToken = responseJSON.getString("publicKeyToken");

        JSONObject paymentJson = responseJSON.getJSONObject("payment");
        countryCode = paymentJson.getString("countryCode");
        reference = paymentJson.getString("reference");
        sessionValidity = paymentJson.getString("sessionValidity");

        JSONObject amountJson = paymentJson.getJSONObject("amount");
        amount = new Amount(amountJson.getLong("value"), amountJson.getString("currency"));

        //Optional fields. Can continue with empty values
        publicKey = responseJSON.optString("publicKey");
        shopperReference = paymentJson.optString("shopperReference");

        //parse payment methods
        paymentMethods = parsePaymentMethods(responseJSON.getJSONArray("paymentMethods"));
        preferredPaymentMethods = parsePreferredPaymentMethods(responseJSON.optJSONArray("recurringDetails"));
    }

    public String getPaymentData() {
        return paymentData;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getGenerationTime() {
        return generationTime;
    }

    public String getInitiationURL() {
        return initiationUrl;
    }

    public String getLogoBaseURL() {
        return logoBaseUrl;
    }

    public String getOrigin() {
        return origin;
    }

    public String getPublicKeyToken() {
        return publicKeyToken;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getReference() {
        return reference;
    }

    public String getSessionValidity() {
        return sessionValidity;
    }

    public String getShopperReference() {
        return shopperReference;
    }

    public List<PaymentMethod> getAvailablePaymentMethods() {
        return paymentMethods;
    }

    public List<PaymentMethod> getPreferredPaymentMethods() {
        return preferredPaymentMethods;
    }

    @NonNull
    private List<PaymentMethod> parsePaymentMethods(final JSONArray jsonArray) throws JSONException {
        final List<PaymentMethod> paymentMethodList = new ArrayList<>();
        if (jsonArray == null) {
            return paymentMethodList;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject paymentMethodJSON = jsonArray.getJSONObject(i);
            if (paymentMethodJSON.has("group")) {
                final String groupType = paymentMethodJSON.getJSONObject("group").getString("type");
                boolean groupExists = false;
                for (final PaymentMethod paymentMethod : paymentMethodList) {
                    if (groupType.equals(paymentMethod.getType())) {
                        groupExists = true;
                        paymentMethod.addMember(PaymentMethod.createPaymentMethod(paymentMethodJSON,
                                getLogoBaseURL()));
                        break;
                    }
                }
                if (!groupExists) {
                    final PaymentMethod containerPaymentMethod = PaymentMethod.createContainerPaymentMethod(
                            paymentMethodJSON, getLogoBaseURL());
                    containerPaymentMethod.addMember(PaymentMethod.createPaymentMethod(paymentMethodJSON,
                            getLogoBaseURL()));
                    paymentMethodList.add(containerPaymentMethod);
                }
            } else {
                paymentMethodList.add(PaymentMethod.createPaymentMethod(paymentMethodJSON, getLogoBaseURL()));
            }
        }
        return paymentMethodList;
    }

    @NonNull
    private List<PaymentMethod> parsePreferredPaymentMethods(final JSONArray jsonArray) throws JSONException {
        final List<PaymentMethod> paymentMethodList = new ArrayList<>();
        if (jsonArray == null) {
            return paymentMethodList;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            paymentMethodList.add(PaymentMethod.createPaymentMethod(jsonArray.getJSONObject(i), getLogoBaseURL(), true));
        }
        return paymentMethodList;
    }

    private JSONObject getJSONResponse(@NonNull final byte[] response) throws JSONException {
        return new JSONObject(new String(response, Charset.forName("UTF-8")));
    }

}
