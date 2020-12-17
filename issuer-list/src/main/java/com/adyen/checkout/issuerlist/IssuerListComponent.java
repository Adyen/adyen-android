/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.adyen.checkout.components.PaymentComponentState;
import com.adyen.checkout.components.base.BasePaymentComponent;
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate;
import com.adyen.checkout.components.model.paymentmethods.Issuer;
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod;
import com.adyen.checkout.components.model.payments.request.PaymentComponentData;

import java.util.ArrayList;
import java.util.List;

public abstract class IssuerListComponent<IssuerListPaymentMethodT extends IssuerListPaymentMethod>
        extends BasePaymentComponent<
        IssuerListConfiguration,
        IssuerListInputData,
        IssuerListOutputData,
        PaymentComponentState<IssuerListPaymentMethodT>
        > {

    private final MutableLiveData<List<IssuerModel>> mIssuersLiveData = new MutableLiveData<>();

    @SuppressWarnings("LambdaLast")
    public IssuerListComponent(@NonNull GenericPaymentMethodDelegate genericPaymentMethodDelegate, @NonNull IssuerListConfiguration configuration) {
        super(genericPaymentMethodDelegate, configuration);
        initIssuers(genericPaymentMethodDelegate.getPaymentMethod().getIssuers());
    }

    MutableLiveData<List<IssuerModel>> getIssuersLiveData() {
        return mIssuersLiveData;
    }

    private void initIssuers(@Nullable List<Issuer> issuerList) {
        if (issuerList != null) {
            final List<IssuerModel> issuerModelList = new ArrayList<>();
            for (Issuer issuer : issuerList) {
                if (!issuer.isDisabled()) {
                    final IssuerModel issuerModel = new IssuerModel(issuer.getId(), issuer.getName());
                    issuerModelList.add(issuerModel);
                }
            }
            mIssuersLiveData.setValue(issuerModelList);
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

        issuerListPaymentMethod.setType(mPaymentMethodDelegate.getPaymentMethodType());
        issuerListPaymentMethod.setIssuer(selectedIssuer != null ? selectedIssuer.getId() : "");

        final boolean isValid = getOutputData().isValid();

        final PaymentComponentData<IssuerListPaymentMethodT> paymentComponentData = new PaymentComponentData<>();
        paymentComponentData.setPaymentMethod(issuerListPaymentMethod);

        return new PaymentComponentState<>(paymentComponentData, isValid);
    }

    @NonNull
    protected abstract IssuerListPaymentMethodT instantiateTypedPaymentMethod();

    @NonNull
    protected String getPaymentMethodType() {
        return mPaymentMethodDelegate.getPaymentMethodType();
    }
}
