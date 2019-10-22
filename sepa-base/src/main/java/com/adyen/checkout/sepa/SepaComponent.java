/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */

package com.adyen.checkout.sepa;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.component.PaymentComponentProviderImpl;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.model.payments.request.SepaPaymentMethod;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public class SepaComponent extends BasePaymentComponent<SepaConfiguration, SepaInputData, SepaOutputData> {
    private static final String TAG = LogUtil.getTag();

    public static final PaymentComponentProvider<SepaComponent, SepaConfiguration> PROVIDER = new PaymentComponentProviderImpl<>(SepaComponent.class);

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.SEPA};

    public SepaComponent(@NonNull PaymentMethod paymentMethod, @NonNull SepaConfiguration configuration) {
        super(paymentMethod, configuration);
    }

    @NonNull
    @Override
    protected SepaOutputData onInputDataChanged(@NonNull SepaInputData inputData) {
        Logger.v(TAG, "onInputDataChanged");
        return new SepaOutputData(inputData.getName(), inputData.getIban());
    }

    @NonNull
    @Override
    protected PaymentComponentState createComponentState() {

        final SepaOutputData sepaOutputData = getOutputData();

        final PaymentComponentData<SepaPaymentMethod> paymentComponentData = new PaymentComponentData<>();
        final SepaPaymentMethod paymentMethod = new SepaPaymentMethod();
        paymentMethod.setType(SepaPaymentMethod.PAYMENT_METHOD_TYPE);

        if (sepaOutputData != null) {
            paymentMethod.setOwnerName(sepaOutputData.getOwnerNameField().getValue());
            paymentMethod.setIbanNumber(sepaOutputData.getIbanNumberField().getValue());
        }

        paymentComponentData.setPaymentMethod(paymentMethod);

        return new PaymentComponentState<>(paymentComponentData, sepaOutputData != null && sepaOutputData.isValid());
    }

    @Nullable
    @Override
    protected SepaOutputData getOutputData() {
        return super.getOutputData();
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }

    @Override
    protected void observeOutputData(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<SepaOutputData> observer) {
        super.observeOutputData(lifecycleOwner, observer);
    }
}
