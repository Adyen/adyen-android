package com.adyen.core.models.paymentdetails;


/**
 * PaymentDetails class for one click credit card payments if only the CVC code is required.
 */
public class CVCOnlyPaymentDetails extends PaymentDetails {

    private static final String CARD_DETAILS_CVC = "cardDetails.cvc";

    /**
     * Create PaymentDetails for recurring one click credit card payment.
     * @param cvc The cvc code collected from the user.
     */
    public CVCOnlyPaymentDetails(final String cvc) {
        map.put(CARD_DETAILS_CVC, cvc);
    }

}
