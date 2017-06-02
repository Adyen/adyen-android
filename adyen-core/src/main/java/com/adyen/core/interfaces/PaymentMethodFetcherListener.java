package com.adyen.core.interfaces;

import com.adyen.core.models.PaymentMethod;

import java.util.List;

/**
 * The listener interface for PaymentMethods fetched event.
 *
 * The classes that implement this listener will be notified when the available payment methods
 * have been fetched and filtered.
 *
 */

public interface PaymentMethodFetcherListener {
    /**
     * The callback for notifying listeners when payment methods are retrieved from server.
     * @param paymentMethodList The list of payment methods.
     */
    void onPaymentMethodsFetched(List<PaymentMethod> paymentMethodList);
}
