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
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.base.BasePaymentComponent;
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate;
import com.adyen.checkout.components.model.paymentmethods.InputDetail;
import com.adyen.checkout.components.model.paymentmethods.Issuer;
import com.adyen.checkout.components.model.paymentmethods.Item;
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod;
import com.adyen.checkout.components.model.payments.request.PaymentComponentData;

import java.util.ArrayList;
import java.util.List;

public abstract class IssuerListComponent<IssuerListPaymentMethodT extends IssuerListPaymentMethod>
        extends BasePaymentComponent<
        IssuerListConfiguration,
        IssuerListInputData,
        IssuerListOutputData,
        GenericComponentState<IssuerListPaymentMethodT>
        > {

    private final MutableLiveData<List<IssuerModel>> mIssuersLiveData = new MutableLiveData<>();

    @SuppressWarnings("LambdaLast")
    public IssuerListComponent(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull GenericPaymentMethodDelegate genericPaymentMethodDelegate,
            @NonNull IssuerListConfiguration configuration
    ) {
        super(savedStateHandle, genericPaymentMethodDelegate, configuration);
        initComponent(genericPaymentMethodDelegate.getPaymentMethod());
    }

    MutableLiveData<List<IssuerModel>> getIssuersLiveData() {
        return mIssuersLiveData;
    }

    private void initComponent(@NonNull PaymentMethod paymentMethod) {
        final List<Issuer> issuersList = paymentMethod.getIssuers();
        if (issuersList != null) {
            initIssuers(issuersList);
        } else {
            initLegacyIssuers(paymentMethod.getDetails());
        }
    }

    private void initIssuers(@NonNull List<Issuer> issuerList) {
        final List<IssuerModel> issuerModelList = new ArrayList<>();
        for (Issuer issuer : issuerList) {
            if (!issuer.isDisabled()) {
                final IssuerModel issuerModel = new IssuerModel(issuer.getId(), issuer.getName());
                issuerModelList.add(issuerModel);
            }
        }
        mIssuersLiveData.setValue(issuerModelList);
    }

    private void initLegacyIssuers(@Nullable List<InputDetail> details) {
        if (details != null) {
            for (InputDetail detail : details) {
                if (detail.getItems() != null) {
                    final List<IssuerModel> issuers = new ArrayList<>();
                    for (Item item : detail.getItems()) {
                        final IssuerModel issuer = new IssuerModel(item.getId(), item.getName());
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
    protected GenericComponentState<IssuerListPaymentMethodT> createComponentState() {
        final IssuerListPaymentMethodT issuerListPaymentMethod = instantiateTypedPaymentMethod();

        final IssuerModel selectedIssuer = getOutputData() != null ? getOutputData().getSelectedIssuer() : null;

        issuerListPaymentMethod.setType(mPaymentMethodDelegate.getPaymentMethodType());
        issuerListPaymentMethod.setIssuer(selectedIssuer != null ? selectedIssuer.getId() : "");

        final boolean isInputValid = getOutputData().isValid();

        final PaymentComponentData<IssuerListPaymentMethodT> paymentComponentData = new PaymentComponentData<>();
        paymentComponentData.setPaymentMethod(issuerListPaymentMethod);

        return new GenericComponentState<>(paymentComponentData, isInputValid, true);
    }

    @NonNull
    protected abstract IssuerListPaymentMethodT instantiateTypedPaymentMethod();

    @NonNull
    protected String getPaymentMethodType() {
        return mPaymentMethodDelegate.getPaymentMethodType();
    }
}
