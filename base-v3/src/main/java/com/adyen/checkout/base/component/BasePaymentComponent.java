/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.base.component;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.adyen.checkout.base.ComponentError;
import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.analytics.AnalyticEvent;
import com.adyen.checkout.base.analytics.AnalyticsDispatcher;
import com.adyen.checkout.base.component.data.input.InputData;
import com.adyen.checkout.base.component.data.output.OutputData;
import com.adyen.checkout.base.component.lifecycle.PaymentComponentViewModel;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.core.api.ThreadManager;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exeption.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public abstract class BasePaymentComponent<ConfigurationT extends Configuration, InputDataT extends InputData, OutputDataT extends OutputData>
        extends PaymentComponentViewModel<ConfigurationT> {

    private static final String TAG = LogUtil.getTag();

    @SuppressWarnings(Lint.SYNTHETIC)
    final MutableLiveData<PaymentComponentState> mPaymentComponentStateLiveData = new MutableLiveData<>();

    private final MutableLiveData<ComponentError> mComponentErrorLiveData = new MutableLiveData<>();

    @NonNull
    private OutputDataT mOutputData;

    private final MutableLiveData<OutputDataT> mOutputLiveData = new MutableLiveData<>();

    private boolean mIsCreatedForDropIn = false;
    private boolean mIsAnalyticsEnabled = true;

    /**
     * Component should not be instantiated directly. Instead use the PROVIDER object.
     *
     * @param paymentMethod {@link PaymentMethod}
     * @param configuration {@link ConfigurationT}
     */
    public BasePaymentComponent(@NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT configuration) {
        super(paymentMethod, configuration);
        assertSupported(paymentMethod);
        mOutputData = createOutputData(paymentMethod);
        mOutputLiveData.setValue(mOutputData);
    }

    /**
     * Receives a net set of {@link InputData} from the user to be processed.
     *
     * @param inputData {@link InputDataT}
     */
    public final void inputDataChanged(@NonNull InputDataT inputData) {
        final OutputDataT newOutputData = onInputDataChanged(inputData);
        if (!mOutputData.equals(newOutputData)) {
            mOutputData = newOutputData;
            mOutputLiveData.setValue(mOutputData);
            notifyStateChanged();
        }
    }

    @NonNull
    protected OutputDataT getOutputData() {
        return mOutputData;
    }

    @NonNull
    protected abstract OutputDataT onInputDataChanged(@NonNull InputDataT inputData);

    private void notifyStateChanged() {
        final PaymentComponentState currentValue = mPaymentComponentStateLiveData.getValue();
        final boolean wasValid = currentValue != null && currentValue.isValid();
        // if last value was valid and new output data become invalid we need to notify observer
        // in any other cases we notify observer when output data is valid.
        final boolean shouldNotify = mOutputData.isValid() || mOutputData.isValid() != wasValid;
        if (shouldNotify) {
            ThreadManager.EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    mPaymentComponentStateLiveData.postValue(createComponentState());
                }
            });
        }
    }

    @Override
    @Nullable
    public PaymentComponentState getState() {
        return mPaymentComponentStateLiveData.getValue();
    }

    @Override
    public void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<PaymentComponentState> observer) {
        mPaymentComponentStateLiveData.observe(lifecycleOwner, observer);
    }

    @Override
    public void observeErrors(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ComponentError> observer) {
        mComponentErrorLiveData.observe(lifecycleOwner, observer);
    }

    protected void notifyException(@NonNull CheckoutException e) {
        Logger.e(TAG, "notifyException - " + e.getMessage());
        mComponentErrorLiveData.postValue(new ComponentError(e));
    }

    @CallSuper
    protected void observeOutputData(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<OutputDataT> observer) {
        // Parent component needs to overrides this for view to have access to the method in the package
        mOutputLiveData.observe(lifecycleOwner, observer);
    }

    @NonNull
    protected abstract OutputDataT createOutputData(@NonNull PaymentMethod paymentMethod);


    @NonNull
    @WorkerThread
    protected abstract PaymentComponentState createComponentState();

    private void assertSupported(@NonNull PaymentMethod paymentMethod) {
        if (!isSupported(paymentMethod)) {
            throw new IllegalArgumentException("Unsupported payment method type " + paymentMethod);
        }
    }

    private boolean isSupported(@NonNull PaymentMethod paymentMethod) {
        return getPaymentMethodType().equals(paymentMethod.getType());
    }

    public void setCreatedForDropIn() {
        mIsCreatedForDropIn = true;
    }

    /**
     * Sets if the analytics events can be sent by the component.
     * Default is True.
     *
     * @param isEnabled Is analytics should be enabled or not.
     */
    public void setAnalyticsEnabled(boolean isEnabled) {
        mIsAnalyticsEnabled = isEnabled;
    }

    /**
     * Send an analytic event about the Component being shown to the user.
     *
     * @param context The context where the component is.
     */
    public void sendAnalyticsEvent(@NonNull Context context) {
        if (mIsAnalyticsEnabled) {
            final AnalyticEvent.Flavor flavor;
            if (mIsCreatedForDropIn) {
                flavor = AnalyticEvent.Flavor.DROPIN;
            } else {
                flavor = AnalyticEvent.Flavor.COMPONENT;
            }

            final AnalyticEvent analyticEvent = AnalyticEvent.create(context, flavor, getPaymentMethodType(), getConfiguration().getShopperLocale());
            AnalyticsDispatcher.dispatchEvent(context, getConfiguration().getEnvironment(), analyticEvent);
        }
    }
}
