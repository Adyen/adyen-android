/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.arch.lifecycle.MutableLiveData;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.api.LogoApi;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.model.paymentmethods.InputDetail;
import com.adyen.checkout.base.model.paymentmethods.Item;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.IssuerListPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class IssuerListComponent<IssuerListPaymentMethodT extends IssuerListPaymentMethod> extends
        BasePaymentComponent<IssuerListConfiguration, IssuerListInputData, IssuerListOutputData> implements
        IssuerLogoCallback.DrawableFetchedCallback {
    private static final String TAG = LogUtil.getTag();

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

    protected void fetchIssuerLogo(@NonNull final String issuerId) {
        Logger.v(TAG, "fetchIssuerLogo - " + issuerId);
        final IssuerLogoCallback callback = new IssuerLogoCallback(issuerId, this);
        final LogoApi logoApi = LogoApi.getInstance(getConfiguration().getEnvironment(), getConfiguration().getDisplayMetrics());
        logoApi.getLogo(getPaymentMethodType(), issuerId, null, callback);
    }

    @Override
    public void onDrawableFetched(@NonNull String id, @Nullable Drawable drawable) {
        Logger.v(TAG, "onDrawableFetched - " + id);
        final IssuerModel issuer = IssuerModel.getFromList(id, mIssuersLiveData.getValue());

        if (issuer != null) {
            if (issuer.getLogo() != drawable && drawable != null) {
                issuer.setLogo(drawable);
                // notify that the content of the array changed
                mIssuersLiveData.setValue(mIssuersLiveData.getValue());
            }
        } else  {
            Logger.e(TAG, "IssuerModel ID has no associated variable - " + id);
        }
    }

    @NonNull
    @Override
    protected IssuerListOutputData createOutputData(@NonNull PaymentMethod paymentMethod) {
        return new IssuerListOutputData(null);
    }

    @NonNull
    @Override
    protected PaymentComponentState<IssuerListPaymentMethodT> createComponentState() {
        final IssuerListPaymentMethodT issuerListPaymentMethod = instantiateTypedPaymentMethod();

        final IssuerModel selectedIssuer = getOutputData().getSelectedIssuer();

        issuerListPaymentMethod.setType(getPaymentMethodType());
        issuerListPaymentMethod.setIssuer(selectedIssuer != null ? selectedIssuer.getId() : "");

        final boolean isValid = getOutputData().isValid();

        final PaymentComponentData<IssuerListPaymentMethodT> paymentComponentData = new PaymentComponentData<>();
        paymentComponentData.setPaymentMethod(issuerListPaymentMethod);

        return new PaymentComponentState<>(paymentComponentData, isValid);
    }

    @NonNull
    protected abstract IssuerListPaymentMethodT instantiateTypedPaymentMethod();

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.d(TAG, "onCleared");
        // cancel all pending logo requests
        LogoApi.getInstance(getConfiguration().getEnvironment(), getConfiguration().getDisplayMetrics()).cancellAll();
    }
}
