/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */

package com.adyen.checkout.wechatpay;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.PaymentComponentProvider;
import com.adyen.checkout.components.base.BasePaymentComponent;
import com.adyen.checkout.components.base.EmptyInputData;
import com.adyen.checkout.components.base.EmptyOutputData;
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate;
import com.adyen.checkout.components.base.PaymentMethodDelegate;
import com.adyen.checkout.components.model.payments.request.GenericPaymentMethod;
import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.components.util.PaymentMethodTypes;

public class WeChatPayComponent
        extends BasePaymentComponent<WeChatPayConfiguration, EmptyInputData, EmptyOutputData, GenericComponentState<GenericPaymentMethod>> {

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
    protected GenericComponentState<GenericPaymentMethod> createComponentState() {
        final GenericPaymentMethod paymentMethodDetails = new GenericPaymentMethod(PaymentMethodTypes.WECHAT_PAY_SDK);
        final PaymentComponentData<GenericPaymentMethod> componentData = new PaymentComponentData<>();
        componentData.setPaymentMethod(paymentMethodDetails);

        return new GenericComponentState<>(componentData, true);
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
