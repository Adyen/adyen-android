/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */

package com.adyen.checkout.blik;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.StoredPaymentComponentProvider;
import com.adyen.checkout.components.base.BasePaymentComponent;
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate;
import com.adyen.checkout.components.base.GenericStoredPaymentComponentProvider;
import com.adyen.checkout.components.base.GenericStoredPaymentDelegate;
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod;
import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.components.util.PaymentMethodTypes;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public class BlikComponent extends BasePaymentComponent<BlikConfiguration, BlikInputData, BlikOutputData, GenericComponentState<BlikPaymentMethod>> {
    private static final String TAG = LogUtil.getTag();

    public static final StoredPaymentComponentProvider<BlikComponent, BlikConfiguration> PROVIDER =
            new GenericStoredPaymentComponentProvider<>(BlikComponent.class);

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.BLIK};

    public BlikComponent(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull GenericPaymentMethodDelegate paymentMethodDelegate,
            @NonNull BlikConfiguration configuration
    ) {
        super(savedStateHandle, paymentMethodDelegate, configuration);
    }

    public BlikComponent(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull GenericStoredPaymentDelegate paymentDelegate,
            @NonNull BlikConfiguration configuration
    ) {
        super(savedStateHandle, paymentDelegate, configuration);
        // TODO: 09/12/2020 move this logic to base component, maybe create the inputdata from the delegate?
        inputDataChanged(new BlikInputData());
    }

    @Override
    public boolean requiresInput() {
        return mPaymentMethodDelegate instanceof GenericPaymentMethodDelegate;
    }

    @NonNull
    @Override
    protected BlikOutputData onInputDataChanged(@NonNull BlikInputData inputData) {
        Logger.v(TAG, "onInputDataChanged");
        return new BlikOutputData(inputData.getBlikCode());
    }

    @NonNull
    @Override
    protected GenericComponentState<BlikPaymentMethod> createComponentState() {

        final BlikOutputData blikOutputData = getOutputData();

        final PaymentComponentData<BlikPaymentMethod> paymentComponentData = new PaymentComponentData<>();
        final BlikPaymentMethod paymentMethod = new BlikPaymentMethod();
        paymentMethod.setType(BlikPaymentMethod.PAYMENT_METHOD_TYPE);

        if (blikOutputData != null) {
            paymentMethod.setBlikCode(blikOutputData.getBlikCodeField().getValue());
        }

        if (mPaymentMethodDelegate instanceof GenericStoredPaymentDelegate) {
            paymentMethod.setStoredPaymentMethodId(
                    ((GenericStoredPaymentDelegate) mPaymentMethodDelegate).getStoredPaymentMethod().getId());
        }

        paymentComponentData.setPaymentMethod(paymentMethod);

        final boolean isInputValid = mPaymentMethodDelegate instanceof GenericStoredPaymentDelegate
                || blikOutputData != null
                && blikOutputData.isValid();

        return new GenericComponentState<>(paymentComponentData, isInputValid, true);
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }
}
