/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.base.component;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.ActionComponent;
import com.adyen.checkout.base.ActionComponentData;
import com.adyen.checkout.base.ComponentError;
import com.adyen.checkout.base.model.payments.response.Action;
import com.adyen.checkout.core.exeption.CheckoutException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class BaseActionComponent extends AndroidViewModel implements ActionComponent {

    private static final String DETAILS_KEY = "details";
    private static final String PAYMENT_DATA_KEY = "paymentData";

    private final MutableLiveData<ActionComponentData> mResultLiveData = new MutableLiveData<>();

    private final MutableLiveData<ComponentError> mErrorParamMutableLiveData = new MutableLiveData<>();

    private String mPaymentData;

    public BaseActionComponent(@NonNull Application application) {
        super(application);
    }

    @Override
    public boolean canHandleAction(@NonNull Action action) {
        return getSupportedActionTypes().contains(action.getType());
    }

    @NonNull
    protected abstract List<String> getSupportedActionTypes();

    @Override
    public void handleAction(@NonNull Activity activity, @NonNull Action action) {
        if (!canHandleAction(action)) {
            throw new CheckoutException("Action type not supported by this component - " + action.getType());
        }
        mPaymentData = action.getPaymentData();
        handleActionInternal(activity, action);
    }

    @Override
    public void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ActionComponentData> observer) {
        mResultLiveData.observe(lifecycleOwner, observer);
    }

    @Override
    public void observeErrors(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ComponentError> observer) {
        mErrorParamMutableLiveData.observe(lifecycleOwner, observer);
    }

    protected abstract void handleActionInternal(@NonNull Activity activity, @NonNull Action action);

    protected void notifyDetails(@NonNull JSONObject details) {

        final JSONObject componentData = new JSONObject();
        try {
            componentData.putOpt(PAYMENT_DATA_KEY, mPaymentData);
            componentData.accumulate(DETAILS_KEY, details);
        } catch (JSONException e) {
            throw new CheckoutException("Unable to create ActionComponentData", e);
        }

        mResultLiveData.setValue(new ActionComponentData(componentData));
    }

    protected void notifyError(@NonNull ComponentError componentError) {
        mErrorParamMutableLiveData.setValue(componentError);
    }
}
