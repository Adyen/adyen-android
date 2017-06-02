package com.adyen.core.models.paymentdetails;


/**
 * {@link PaymentDetails} class for credit card Payments.
 * This class contains only encrypted card data.
 */
public class CreditCardPaymentDetails extends PaymentDetails {

    private static final String ADDITIONAL_DATA_CARD = "additionalData.card.encrypted.json";
    private static final String STORE_DETAILS = "storeDetails";

    /**
     * Create {@link PaymentDetails} for credit card payments.
     * @param encryptedData The encrypted credit card data
     * @param storeDetails If the card details should be stored.
     */
    public CreditCardPaymentDetails(final String encryptedData, final boolean storeDetails) {
        map.put(ADDITIONAL_DATA_CARD, encryptedData);
        map.put(STORE_DETAILS, storeDetails);
    }
}
