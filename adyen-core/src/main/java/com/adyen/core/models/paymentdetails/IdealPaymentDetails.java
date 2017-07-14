package com.adyen.core.models.paymentdetails;

import java.util.Collection;

/**
 * PaymentDetails class for sepa direct debit payments.
 */
public class IdealPaymentDetails extends PaymentDetails {

    private static final String IDEAL_ISSUER = "idealIssuer";

    public IdealPaymentDetails(Collection<InputDetail> inputDetails) {
        super(inputDetails);
    }

    public boolean fillIssuer(final String issuerId) {
        return super.fill(IDEAL_ISSUER, issuerId);
    }

    public boolean fillIssuer(final InputDetail.Item issuerItem) {
        return super.fill(IDEAL_ISSUER, issuerItem.getId());
    }

}
