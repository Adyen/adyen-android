package com.adyen.core.interfaces;

import com.adyen.core.models.PaymentMethod;

/**
 * Listener that gets notified when calling
 * {@link com.adyen.core.PaymentRequest#deletePreferredPaymentMethod(PaymentMethod, DeletePreferredPaymentMethodListener)}.
 */

public interface DeletePreferredPaymentMethodListener {

    void onSuccess();
    void onFail();
}
