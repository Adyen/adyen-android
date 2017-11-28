package com.adyen.core.models.paymentdetails;

import java.util.Collection;

/**
 * PaymentDetails class for Qiwi wallet payments.
 */
public class QiwiWalletPaymentDetails extends PaymentDetails {

    private static final String QIWI_TELEPHONE_NUMBER_PREFIX = "qiwiwallet.telephoneNumberPrefix";
    private static final String QIWI_TELEPHONE_NUMBER = "qiwiwallet.telephoneNumber";


    public QiwiWalletPaymentDetails(Collection<InputDetail> inputDetails) {
        super(inputDetails);
    }

    public boolean fillTelephoneNumber(final String countryCode, final String telephoneNumber) {
        return super.fill(QIWI_TELEPHONE_NUMBER, telephoneNumber)
                && super.fill(QIWI_TELEPHONE_NUMBER_PREFIX, countryCode);
    }

}
