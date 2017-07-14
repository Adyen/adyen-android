package com.adyen.core.models.paymentdetails;

import java.util.Collection;

/**
 * PaymentDetails class for sepa direct debit payments.
 */
public class SepaDirectDebitPaymentDetails extends PaymentDetails {

    private static final String SEPA_IBAN_NUMBER = "sepa.ibanNumber";
    private static final String SEPA_IBAN_OWNER = "sepa.ownerName";


    public SepaDirectDebitPaymentDetails(Collection<InputDetail> inputDetails) {
        super(inputDetails);
    }

    public boolean fillIban(final String iban) {
        return super.fill(SEPA_IBAN_NUMBER, iban);
    }

    public boolean fillOwner(final String owner) {
        return super.fill(SEPA_IBAN_OWNER, owner);
    }
}
