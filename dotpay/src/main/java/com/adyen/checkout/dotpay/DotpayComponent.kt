/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.dotpay;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.PaymentComponentProvider;
import com.adyen.checkout.components.base.GenericPaymentComponentProvider;
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate;
import com.adyen.checkout.components.model.payments.request.DotpayPaymentMethod;
import com.adyen.checkout.components.util.PaymentMethodTypes;
import com.adyen.checkout.issuerlist.IssuerListComponent;
import com.adyen.checkout.issuerlist.IssuerListInputData;
import com.adyen.checkout.issuerlist.IssuerListOutputData;

/**
 * PaymentComponent to handle iDeal payments.
 */
public final class DotpayComponent extends IssuerListComponent<DotpayPaymentMethod> {

    public static final PaymentComponentProvider<DotpayComponent, DotpayConfiguration> PROVIDER =
            new GenericPaymentComponentProvider<>(DotpayComponent.class);

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.DOTPAY};

    public DotpayComponent(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull GenericPaymentMethodDelegate paymentMethodDelegate,
            @NonNull DotpayConfiguration configuration
    ) {
        super(savedStateHandle, paymentMethodDelegate, configuration);
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }

    @Override
    @NonNull
    protected IssuerListOutputData onInputDataChanged(@NonNull IssuerListInputData inputData) {
        return super.onInputDataChanged(inputData);
    }

    @NonNull
    @Override
    protected DotpayPaymentMethod instantiateTypedPaymentMethod() {
        return new DotpayPaymentMethod();
    }

}
