package com.adyen.core.models.paymentdetails;

import java.util.Collection;

/**
 * PaymentDetails class for one click credit card payments if only the CVC code is required.
 */
public class CVCOnlyPaymentDetails extends PaymentDetails {

    public static final String CARD_DETAILS_CVC = "cardDetails.cvc";

    public CVCOnlyPaymentDetails(Collection<InputDetail> inputDetails) {
        super(inputDetails);
    }

    public boolean fillCvc(final String cvc) {
        return super.fill(CARD_DETAILS_CVC, cvc);
    }
}
