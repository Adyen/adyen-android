package com.adyen.core.internals;

import android.support.annotation.NonNull;

import com.adyen.core.models.PaymentModule;
import com.adyen.core.services.InputFieldsService;

public class InputFieldsServiceFactory {

    @NonNull
    public InputFieldsService getService(@NonNull PaymentModule paymentModule)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (InputFieldsService) Class.forName(paymentModule.getInputFieldsServiceName()).newInstance();
    }

}
