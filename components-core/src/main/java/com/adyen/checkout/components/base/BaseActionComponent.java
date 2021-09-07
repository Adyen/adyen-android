/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.components.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.adyen.checkout.components.ActionComponentData;
import com.adyen.checkout.components.ComponentError;
import com.adyen.checkout.components.base.lifecycle.ActionComponentViewModel;
import com.adyen.checkout.components.model.payments.response.Action;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import org.json.JSONObject;

public abstract class BaseActionComponent<ConfigurationT extends Configuration> extends ActionComponentViewModel<ConfigurationT> {
    private static final String TAG = LogUtil.getTag();

    private static final String PAYMENT_DATA_KEY = "payment_data";

    private final MutableLiveData<ActionComponentData> mResultLiveData = new MutableLiveData<>();

    private final MutableLiveData<ComponentError> mErrorMutableLiveData = new MutableLiveData<>();

    private String mPaymentData;

    public BaseActionComponent(@NonNull Application application, @NonNull ConfigurationT configuration) {
        super(application, configuration);
    }

    @Override
    public void handleAction(@NonNull Activity activity, @NonNull Action action) {
        if (!canHandleAction(action)) {
            notifyException(new ComponentException("Action type not supported by this component - " + action.getType()));
            return;
        }

        mPaymentData = action.getPaymentData();
        try {
            handleActionInternal(activity, action);
        } catch (ComponentException e) {
            notifyException(e);
        }
    }

    @Override
    public void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ActionComponentData> observer) {
        mResultLiveData.observe(lifecycleOwner, observer);
    }

    @Override
    public void removeObservers(@NonNull LifecycleOwner lifecycleOwner) {
        mResultLiveData.removeObservers(lifecycleOwner);
    }

    @Override
    public void observeErrors(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ComponentError> observer) {
        mErrorMutableLiveData.observe(lifecycleOwner, observer);
    }

    @Override
    public void removeErrorObservers(@NonNull LifecycleOwner lifecycleOwner) {
        mErrorMutableLiveData.removeObservers(lifecycleOwner);
    }

    /**
     * Call this method to save the current data of the Component to the Bundle from {@link Activity#onSaveInstanceState(Bundle)}.
     *
     * @param bundle The bundle to save the sate into.
     */
    public void saveState(@Nullable Bundle bundle) {
        if (bundle != null && !TextUtils.isEmpty(mPaymentData)) {
            if (bundle.containsKey(PAYMENT_DATA_KEY)) {
                Logger.d(TAG, "bundle already has paymentData, overriding");
            }
            bundle.putString(PAYMENT_DATA_KEY, mPaymentData);
        }
    }

    /**
     * Call this method to restore the current data of the Component from the savedInstanceState Bundle from {@link Activity#onCreate(Bundle)}}.
     *
     * @param bundle The bundle to restore the sate from.
     */
    public void restoreState(@Nullable Bundle bundle) {
        if (bundle != null && bundle.containsKey(PAYMENT_DATA_KEY) && TextUtils.isEmpty(mPaymentData)) {
            mPaymentData = bundle.getString(PAYMENT_DATA_KEY);
        }
    }

    protected abstract void handleActionInternal(@NonNull Activity activity, @NonNull Action action) throws ComponentException;

    protected void notifyDetails(@NonNull JSONObject details) throws ComponentException {
        final ActionComponentData actionComponentData = new ActionComponentData();
        actionComponentData.setDetails(details);
        actionComponentData.setPaymentData(mPaymentData);

        mResultLiveData.setValue(actionComponentData);
    }

    protected void notifyException(@NonNull CheckoutException e) {
        mErrorMutableLiveData.postValue(new ComponentError(e));
    }

    @Nullable
    protected String getPaymentData() {
        return mPaymentData;
    }

    protected void setPaymentData(@Nullable String paymentData) {
        mPaymentData = paymentData;
    }
}
