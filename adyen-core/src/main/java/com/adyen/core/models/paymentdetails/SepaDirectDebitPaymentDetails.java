package com.adyen.core.models.paymentdetails;


/**
 * PaymentDetails class for sepa direct debit payments.
 */
public class SepaDirectDebitPaymentDetails extends PaymentDetails {

    private static final String SEPA_IBAN_NUMBER = "sepa.ibanNumber";
    private static final String SEPA_IBAN_OWNER = "sepa.ownerName";

    /**
     * Create Sepa direct debit payment details.
     * @param ibanNumber The full iban number (without whitespaces).
     * @param ownerName The name of the account holder.
     */
    public SepaDirectDebitPaymentDetails(String ibanNumber, String ownerName) {
        map.put(SEPA_IBAN_NUMBER, ibanNumber);
        map.put(SEPA_IBAN_OWNER, ownerName);
    }

}
