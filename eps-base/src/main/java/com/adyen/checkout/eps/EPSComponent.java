/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.eps;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.PaymentComponentProviderImpl;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.EPSPaymentMethod;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.issuerlist.IssuerListComponent;
import com.adyen.checkout.issuerlist.IssuerListInputData;
import com.adyen.checkout.issuerlist.IssuerListOutputData;

/**
 * PaymentComponent to handle iDeal payments.
 */
@SuppressWarnings("AbbreviationAsWordInName")
public final class EPSComponent extends IssuerListComponent<EPSPaymentMethod> {

    public static final PaymentComponentProvider<EPSComponent, EPSConfiguration> PROVIDER =
            new PaymentComponentProviderImpl<>(EPSComponent.class);

    public EPSComponent(@NonNull PaymentMethod paymentMethod, @NonNull EPSConfiguration configuration) {
        super(paymentMethod, configuration);
    }

    @NonNull
    @Override
    public String getPaymentMethodType() {
        return PaymentMethodTypes.EPS;
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
    protected EPSPaymentMethod instantiateTypedPaymentMethod() {
        return new EPSPaymentMethod();
    }

}
