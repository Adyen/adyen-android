package com.adyen.core.models;

import android.support.annotation.Nullable;

import com.adyen.core.models.paymentdetails.InputDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The class that contains detailed payment method information.
 */
@SuppressWarnings("unused")
public final class PaymentMethod implements Serializable {

    /**
     * Constants for several popular payment methods.
     */
    public static class Type {
        public static final String CARD = "card";
        public static final String IDEAL = "ideal";
        public static final String SEPA_DIRECT_DEBIT = "sepadirectdebit";
        public static final String PAYPAL = "paypal";
    }

    private static final long serialVersionUID = 2587948839462686004L;
    // Mandatory fields.
    private String name;
    private String type;
    private String paymentMethodData;

    /**
     * Set to true iff this is a recurring (oneClick) {@link PaymentMethod}.
     */
    private boolean isOneClick;

    // Optional fields.
    private Configuration configuration;

    // methods (e.g. encrypted card data).
    private Card card;

    // For the grouped payment methods.
    private List<PaymentMethod> memberPaymentMethods;

    private String logoUrl;

    private Collection<InputDetail> inputDetails;

    private PaymentMethod() {

    }

    public Collection<InputDetail> getInputDetails() {
        return this.inputDetails;
    }

    static PaymentMethod createPaymentMethod(final JSONObject paymentMethodJSON, final String logoBaseUrl)
            throws JSONException {
        return createPaymentMethod(paymentMethodJSON, logoBaseUrl, false);
    }

    static PaymentMethod createPaymentMethod(final JSONObject paymentMethodJSON, final String logoBaseUrl, final boolean isOneClick)
            throws JSONException {
        final PaymentMethod paymentMethod = new PaymentMethod();

        paymentMethod.isOneClick = isOneClick;

        paymentMethod.type = paymentMethodJSON.getString("type");
        paymentMethod.name = paymentMethodJSON.getString("name");
        paymentMethod.paymentMethodData = paymentMethodJSON.getString("paymentMethodData");
        paymentMethod.logoUrl = logoBaseUrl + paymentMethod.getType() + ".png";

        final JSONArray inputDetails = paymentMethodJSON.optJSONArray("inputDetails");
        if (inputDetails != null) {
            paymentMethod.inputDetails = parseInputDetails(inputDetails);
        }

        if (!paymentMethodJSON.isNull("card")) {
            final JSONObject card = paymentMethodJSON.getJSONObject("card");
            paymentMethod.name = "•••• " + card.getString("number");
            paymentMethod.card = new Card(card);
        }
        if (!paymentMethodJSON.isNull("configuration")) {
            final JSONObject configurationJson = paymentMethodJSON.getJSONObject("configuration");
            PaymentMethod.Configuration configuration = new Configuration();
            configuration.merchantId = configurationJson.optString("merchantIdentifier");
            configuration.merchantName = configurationJson.optString("merchantName");
            configuration.publicKey = configurationJson.optString("publicKey").replaceAll("\\r\\n", "");
            paymentMethod.configuration = configuration;
        }

        return paymentMethod;
    }

    static PaymentMethod createContainerPaymentMethod(final JSONObject paymentMethodJSON, final String logoUrl)
            throws JSONException {
        final PaymentMethod paymentMethod = new PaymentMethod();

        final JSONObject group = paymentMethodJSON.getJSONObject("group");
        paymentMethod.type = group.getString("type");
        paymentMethod.name = group.getString("name");
        paymentMethod.logoUrl = logoUrl + group.getString("type") + ".png";
        paymentMethod.paymentMethodData = group.getString("paymentMethodData");

        final JSONArray inputDetails = paymentMethodJSON.optJSONArray("inputDetails");
        if (inputDetails != null) {
            paymentMethod.inputDetails = parseInputDetails(inputDetails);
        }

        return paymentMethod;
    }

    private static Collection<InputDetail> parseInputDetails(final JSONArray inputDetailsJson) throws JSONException {
        Collection<InputDetail> inputDetails = new ArrayList<>();
        for (int r = 0; r < inputDetailsJson.length(); r++) {
            final JSONObject field = inputDetailsJson.optJSONObject(r);

            if (field != null) {
                InputDetail inputDetail = InputDetail.fromJson(field);
                inputDetails.add(InputDetail.fromJson(field));
            }
        }
        return inputDetails;
    }

    /**
     * Returns a payment module for the selected payment method.
     * If the selected payment method does not require a module, this method returns null.
     * @return the payment module if the selected method requires a module, otherwise null.
     */
    @Nullable
    public PaymentModule getPaymentModule() {
        for (PaymentModule module : PaymentModule.values()) {
            if (module.toString().equals(type)) {
                return module;
            }
        }
        return null;
    }

    /**
     * Get the saved card details in case this is a recurring payment method.
     * @return The saved card. This is null if this payment method is not recurring or a card.
     */
    @Nullable
    public Card getSavedCard() {
        return card;
    }

    /**
     * Get the type of the payment method.
     * @return The type in a string format.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the name of the payment method.
     * @return The name in a string format.
     */
    public String getName() {
        return name;
    }

    public String getPaymentMethodData() {
        return paymentMethodData;
    }

    /**
     * Get the URL of the payment method's logo.
     * @return The logo URL.
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * Get the member of this {@link PaymentMethod}.
     * For example, for "card" the members can be the specific card types (e.g. "visa", "maestro", ...).
     * @return The list with member payment methods. Can be null if no member payment methods exist.
     */
    public List<PaymentMethod> getMemberPaymentMethods() {
        return memberPaymentMethods;
    }

    /**
     * The method to determine if a redirect is required for this payment method.
     * @return True if a redirect is required, otherwise false
     */
    public boolean isRedirectMethod() {
        return (this.inputDetails == null || this.inputDetails.isEmpty()) && !this.isOneClick;
    }

    /**
     * Check wether this is a recurring (oneClick) {@link PaymentMethod}.
     * @return true iff this is a recurring (oneClick) {@link PaymentMethod}.
     */
    public boolean isOneClick() {
        return isOneClick;
    }

    /**
     * Get the merchant configuration if it was provided with the payment method.
     * @return The merchant configuration, can be null.
     */
    public @Nullable Configuration getConfiguration() {
        return configuration;
    }

    void addMember(final PaymentMethod paymentMethod) {
        if (memberPaymentMethods == null) {
            memberPaymentMethods = new CopyOnWriteArrayList<>();
        }
        memberPaymentMethods.add(paymentMethod);
    }

    /**
     * Contains the saved details about a credit card used in the past.
     */
    public static final class Card implements Serializable {
        private String expiryMonth;
        private String expiryYear;
        private String holderName;
        private String number;

        private Card(JSONObject jsonObject) throws JSONException {
            this.expiryMonth = jsonObject.getString("expiryMonth");
            this.expiryYear = jsonObject.getString("expiryYear");
            this.number = jsonObject.getString("number");

            this.holderName = jsonObject.optString("holderName");

        }

        /**
         * @return The card expiry month (numbers from 1 to 12).
         */
        public String getExpiryMonth() {
            return expiryMonth;
        }

        /**
         * @return The card expiry year.
         */
        public String getExpiryYear() {
            return expiryYear;
        }

        /**
         * @return The name of the card holder.
         */
        public String getHolderName() {
            return holderName;
        }

        /**
         * @return The last four digits of the card number.
         */
        public String getNumber() {
            return number;
        }
    }

    /**
     * The merchant configuration saved with the payment method.
     */
    public static final class Configuration implements Serializable {

        private static final long serialVersionUID = -3525650201514031845L;
        private String merchantId;
        private String merchantName;
        private String publicKey;

        private Configuration() { }

        public String getMerchantId() {
            return merchantId;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public String getPublicKey() {
            return publicKey;
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PaymentMethod)) {
            return false;
        }
        return ((PaymentMethod) obj).getPaymentMethodData().equals(this.paymentMethodData);
    }

    @Override
    public int hashCode() {
        return this.paymentMethodData.hashCode();
    }

    /**
     * Legacy method to get the deprecated requiredFields map.
     * @return Map<String, Object> that can be filled out and passed to the old interface.
     */
    @Deprecated
    public Map<String, Object> getRequiredFieldsMap() {
        Map<String, Object> requiredFields = new HashMap<>();
        for (InputDetail inputDetail : inputDetails) {
            requiredFields.put(inputDetail.getKey(), null);
        }
        return requiredFields;
    }
}
