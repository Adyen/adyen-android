/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.ViewableComponent;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.model.paymentmethods.InputDetail;
import com.adyen.checkout.base.model.paymentmethods.Item;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.IssuerListPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;

import java.util.ArrayList;
import java.util.List;

public abstract class IssuerListComponent<IssuerListPaymentMethodT extends IssuerListPaymentMethod>
        extends BasePaymentComponent<IssuerListConfiguration, IssuerListInputData, IssuerListOutputData, PaymentComponentState>
        implements ViewableComponent<IssuerListOutputData, IssuerListConfiguration, PaymentComponentState> {

    private final MutableLiveData<List<IssuerModel>> mIssuersLiveData = new MutableLiveData<>();

    public IssuerListComponent(@NonNull PaymentMethod paymentMethod, @NonNull IssuerListConfiguration configuration) {
        super(paymentMethod, configuration);
        initIssuers(paymentMethod.getDetails());
    }

    MutableLiveData<List<IssuerModel>> getIssuersLiveData() {
        return mIssuersLiveData;
    }

    private void initIssuers(@Nullable List<InputDetail> details) {
        if (details != null) {
            for (InputDetail detail : details) {
                if (detail.getItems() != null) {
                    final List<IssuerModel> issuers = new ArrayList<>();
                    for (Item item : detail.getItems()) {

                        final IssuerModel issuer = new IssuerModel(item);
                        issuers.add(issuer);
                    }
                    mIssuersLiveData.setValue(issuers);
                }
            }
        }
    }

    @Override
    @NonNull
    protected IssuerListOutputData onInputDataChanged(@NonNull IssuerListInputData inputData) {
        // can also reuse instance if we implement equals properly
        return new IssuerListOutputData(inputData.getSelectedIssuer());
    }

    @NonNull
    @Override
    protected PaymentComponentState<IssuerListPaymentMethodT> createComponentState() {
        final IssuerListPaymentMethodT issuerListPaymentMethod = instantiateTypedPaymentMethod();

        final IssuerModel selectedIssuer = getOutputData() != null ? getOutputData().getSelectedIssuer() : null;

        issuerListPaymentMethod.setType(getPaymentMethod().getType());
        issuerListPaymentMethod.setIssuer(selectedIssuer != null ? selectedIssuer.getId() : "");

        final boolean isValid = getOutputData().isValid();

        final PaymentComponentData<IssuerListPaymentMethodT> paymentComponentData = new PaymentComponentData<>();
        paymentComponentData.setPaymentMethod(issuerListPaymentMethod);

        return new PaymentComponentState<>(paymentComponentData, isValid);
    }

    @NonNull
    protected abstract IssuerListPaymentMethodT instantiateTypedPaymentMethod();
}
