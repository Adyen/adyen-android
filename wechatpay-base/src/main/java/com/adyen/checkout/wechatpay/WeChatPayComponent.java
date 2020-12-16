/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */

package com.adyen.checkout.wechatpay;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.component.EmptyInputData;
import com.adyen.checkout.base.component.EmptyOutputData;
import com.adyen.checkout.base.component.GenericPaymentMethodDelegate;
import com.adyen.checkout.base.component.PaymentMethodDelegate;
import com.adyen.checkout.base.model.payments.request.GenericPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.util.PaymentMethodTypes;

public class WeChatPayComponent
        extends BasePaymentComponent<WeChatPayConfiguration, EmptyInputData, EmptyOutputData, PaymentComponentState<GenericPaymentMethod>> {

    public static final PaymentComponentProvider<WeChatPayComponent, WeChatPayConfiguration> PROVIDER = new WeChatPayProvider();

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.WECHAT_PAY_SDK};

    /**
     * Component should not be instantiated directly. Instead use the PROVIDER object.
     *
     * @param paymentMethodDelegate {@link PaymentMethodDelegate}
     * @param configuration {@link WeChatPayConfiguration}
     */
    public WeChatPayComponent(@NonNull GenericPaymentMethodDelegate paymentMethodDelegate, @NonNull WeChatPayConfiguration configuration) {
        super(paymentMethodDelegate, configuration);
        onInputDataChanged(new EmptyInputData());
    }

    @NonNull
    @Override
    protected EmptyOutputData onInputDataChanged(@NonNull EmptyInputData inputData) {
        return new EmptyOutputData();
    }

    @NonNull
    @Override
    protected PaymentComponentState<GenericPaymentMethod> createComponentState() {
        final GenericPaymentMethod paymentMethodDetails = new GenericPaymentMethod(PaymentMethodTypes.WECHAT_PAY_SDK);
        final PaymentComponentData<GenericPaymentMethod> componentData = new PaymentComponentData<>();
        componentData.setPaymentMethod(paymentMethodDetails);

        return new PaymentComponentState<>(componentData, true);
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }

    @Override
    public boolean requiresInput() {
        return false;
    }
}
