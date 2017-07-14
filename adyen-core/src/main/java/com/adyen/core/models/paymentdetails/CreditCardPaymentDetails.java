package com.adyen.core.models.paymentdetails;

import java.util.Collection;

/**
 * {@link PaymentDetails} class for credit card Payments.
 * This class contains only encrypted card data.
 */
public class CreditCardPaymentDetails extends PaymentDetails {

    public static final String ADDITIONAL_DATA_CARD = "additionalData.card.encrypted.json";
    public static final String STORE_DETAILS = "storeDetails";

    public CreditCardPaymentDetails(Collection<InputDetail> inputDetails) {
        super(inputDetails);
    }

    public boolean fillCardToken(final String cardToken) {
        return super.fill(ADDITIONAL_DATA_CARD, cardToken);
    }

    public boolean fillStoreDetails(final boolean storeDetails) {
        return super.fill(STORE_DETAILS, storeDetails);
    }

}
