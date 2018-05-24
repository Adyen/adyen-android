package com.adyen.core.interfaces;

/**
 * Communicates the response from checking if the selected payment method is available for the merchant account.
 * Where applicable (eg. Samsung Pay) there is also a check on the current device for the availability of selected
 * payment method
 */
public interface PaymentMethodAvailabilityCallback {

    /**
     * The callback for notifying the client that checking for availability has been successfully completed.
     * This doesn't mean that the method is available. The result is passed as an argument.
     * @param isAvailable true if the payment method is available
     */
    void onSuccess(boolean isAvailable);

    /**
     * The callback for notifying the client that checking the availability could not be done.
     *
     * @param e error caused by the availability request
     */
    void onFail(Throwable e);

}
