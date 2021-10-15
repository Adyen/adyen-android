/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */

package com.adyen.checkout.sepa;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.PaymentComponentProvider;
import com.adyen.checkout.components.base.BasePaymentComponent;
import com.adyen.checkout.components.base.GenericPaymentComponentProvider;
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate;
import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod;
import com.adyen.checkout.components.util.PaymentMethodTypes;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public class SepaComponent extends BasePaymentComponent<SepaConfiguration, SepaInputData, SepaOutputData, GenericComponentState<SepaPaymentMethod>> {
    private static final String TAG = LogUtil.getTag();

    public static final PaymentComponentProvider<SepaComponent, SepaConfiguration> PROVIDER =
            new GenericPaymentComponentProvider<>(SepaComponent.class);

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.SEPA};

    public SepaComponent(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull GenericPaymentMethodDelegate paymentMethodDelegate,
            @NonNull SepaConfiguration configuration
    ) {
        super(savedStateHandle, paymentMethodDelegate, configuration);
    }

    @NonNull
    @Override
    protected SepaOutputData onInputDataChanged(@NonNull SepaInputData inputData) {
        Logger.v(TAG, "onInputDataChanged");
        return new SepaOutputData(inputData.getName(), inputData.getIban());
    }

    @NonNull
    @Override
    protected GenericComponentState<SepaPaymentMethod> createComponentState() {

        final SepaOutputData sepaOutputData = getOutputData();

        final PaymentComponentData<SepaPaymentMethod> paymentComponentData = new PaymentComponentData<>();
        final SepaPaymentMethod paymentMethod = new SepaPaymentMethod();
        paymentMethod.setType(SepaPaymentMethod.PAYMENT_METHOD_TYPE);

        if (sepaOutputData != null) {
            paymentMethod.setOwnerName(sepaOutputData.getOwnerNameField().getValue());
            paymentMethod.setIban(sepaOutputData.getIbanNumberField().getValue());
        }

        paymentComponentData.setPaymentMethod(paymentMethod);

        return new GenericComponentState<>(paymentComponentData, sepaOutputData != null && sepaOutputData.isValid(), true);
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }
}
