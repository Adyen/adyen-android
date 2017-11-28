package com.adyen.core.models.paymentdetails;

import java.util.Collection;

/**
 * PaymentDetails class for issuer selection.
 */
public class IssuerSelectionPaymentDetails extends PaymentDetails {

    public static final String ISSUER = "issuer";

    public IssuerSelectionPaymentDetails(Collection<InputDetail> inputDetails) {
        super(inputDetails);
    }

    public boolean fillIssuer(final String issuerId) {
        for (InputDetail inputDetail: getInputDetails()) {
            if (IdealPaymentDetails.IDEAL_ISSUER.equalsIgnoreCase(inputDetail.getKey())
                    || ISSUER.equalsIgnoreCase(inputDetail.getKey())) {
                return super.fill(inputDetail.getKey(), issuerId);
            }
        }
        return false;
    }

    public boolean fillIssuer(final InputDetail.Item issuerItem) {
        return fillIssuer(issuerItem.getId());
    }

}
