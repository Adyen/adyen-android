/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.ideal;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.PaymentComponentProviderImpl;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.IdealPaymentMethod;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.issuerlist.IssuerListComponent;
import com.adyen.checkout.issuerlist.IssuerListInputData;
import com.adyen.checkout.issuerlist.IssuerListOutputData;

/**
 * PaymentComponent to handle iDeal payments.
 */
public final class IdealComponent extends IssuerListComponent<IdealPaymentMethod> {

    public static final PaymentComponentProvider<IdealComponent, IdealConfiguration> PROVIDER =
            new PaymentComponentProviderImpl<>(IdealComponent.class);

    public IdealComponent(@NonNull PaymentMethod paymentMethod, @NonNull IdealConfiguration configuration) {
        super(paymentMethod, configuration);
    }

    @NonNull
    @Override
    public String getPaymentMethodType() {
        return PaymentMethodTypes.IDEAL;
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

    @SuppressWarnings("PMD.UselessOverridingMethod")
    @Override
    protected void fetchIssuerLogo(@NonNull final String issuerId) {
        super.fetchIssuerLogo(issuerId);
    }

    @NonNull
    @Override
    protected IdealPaymentMethod instantiateTypedPaymentMethod() {
        return new IdealPaymentMethod();
    }

}
