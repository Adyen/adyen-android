package com.adyen.core.internals;

import android.support.annotation.NonNull;

import com.adyen.core.models.PaymentModule;
import com.adyen.core.services.PaymentMethodService;

public class PaymentMethodServiceFactory {

    @NonNull
    public PaymentMethodService getService(@NonNull PaymentModule paymentModule) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        return (PaymentMethodService) Class.forName(paymentModule.getServiceName()).newInstance();
    }

}
