package com.adyen.core.models.paymentdetails;

import com.adyen.core.models.Issuer;


/**
 * PaymentDetails class for PaymentMethod ideal.
 */
public class IdealPaymentDetails extends PaymentDetails {

    private static final String IDEAL_ISSUER = "idealIssuer";

    /**
     * Create PaymentDetails for PaymentMethod ideal.
     * @param issuerId String of the issuer id.
     */
    public IdealPaymentDetails(final String issuerId) {
        map.put(IDEAL_ISSUER, issuerId);
    }

    /**
     * Create PaymentDetails for PaymentMethod ideal.
     * @param issuer The selected {@link Issuer} to create the PaymentDetails from.
     */
    public IdealPaymentDetails(final Issuer issuer) {
        map.put(IDEAL_ISSUER, issuer.getIssuerId());
    }

}
