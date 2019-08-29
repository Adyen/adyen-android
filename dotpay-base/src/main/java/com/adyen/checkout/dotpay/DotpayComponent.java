/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.dotpay;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.PaymentComponentProviderImpl;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.DotpayPaymentMethod;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.issuerlist.IssuerListComponent;
import com.adyen.checkout.issuerlist.IssuerListInputData;
import com.adyen.checkout.issuerlist.IssuerListOutputData;

/**
 * PaymentComponent to handle iDeal payments.
 */
public final class DotpayComponent extends IssuerListComponent<DotpayPaymentMethod> {

    public static final PaymentComponentProvider<DotpayComponent, DotpayConfiguration> PROVIDER =
            new PaymentComponentProviderImpl<>(DotpayComponent.class);

    public DotpayComponent(@NonNull PaymentMethod paymentMethod, @NonNull DotpayConfiguration configuration) {
        super(paymentMethod, configuration);
    }

    @NonNull
    @Override
    public String getPaymentMethodType() {
        return PaymentMethodTypes.DOTPAY;
    }

    @Override
    protected void observeOutputData(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<IssuerListOutputData> observer) {
        super.observeOutputData(lifecycleOwner, observer);
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
