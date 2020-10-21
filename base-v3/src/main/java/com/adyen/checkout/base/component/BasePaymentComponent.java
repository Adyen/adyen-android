/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.base.component;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;

import com.adyen.checkout.base.ComponentError;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.ViewableComponent;
import com.adyen.checkout.base.analytics.AnalyticEvent;
import com.adyen.checkout.base.analytics.AnalyticsDispatcher;
import com.adyen.checkout.base.component.lifecycle.PaymentComponentViewModel;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.core.api.ThreadManager;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public abstract class BasePaymentComponent<
            ConfigurationT extends Configuration,
            InputDataT extends InputData,
            OutputDataT extends OutputData,
            ComponentStateT extends PaymentComponentState>
        extends PaymentComponentViewModel<ConfigurationT, ComponentStateT>
        implements ViewableComponent<OutputDataT, ConfigurationT, ComponentStateT> {

    private static final String TAG = LogUtil.getTag();

    @SuppressWarnings(Lint.SYNTHETIC)
    final MutableLiveData<ComponentStateT> mPaymentComponentStateLiveData = new MutableLiveData<>();

    private final MutableLiveData<ComponentError> mComponentErrorLiveData = new MutableLiveData<>();

    @Nullable
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
    }

    @SuppressWarnings("MissingDeprecated")
    @Deprecated
    @NonNull
    @Override
    public String getPaymentMethodType() {
        if (getSupportedPaymentMethodTypes().length > 0) {
            return getSupportedPaymentMethodTypes()[0];
        } else {
            throw new CheckoutException("Component supported types is empty");
        }
    }

    @Override
    public void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ComponentStateT> observer) {
        mPaymentComponentStateLiveData.observe(lifecycleOwner, observer);
    }

    @Override
    public void observeErrors(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ComponentError> observer) {
        mComponentErrorLiveData.observe(lifecycleOwner, observer);
    }

    @Override
    @Nullable
    public PaymentComponentState getState() {
        return mPaymentComponentStateLiveData.getValue();
    }

    /**
     * Receives a set of {@link InputData} from the user to be processed.
     *
     * @param inputData {@link InputDataT}
     */
    public final void inputDataChanged(@NonNull InputDataT inputData) {
        final OutputDataT newOutputData = onInputDataChanged(inputData);
        if (!newOutputData.equals(mOutputData)) {
            mOutputData = newOutputData;
            mOutputLiveData.setValue(mOutputData);
            notifyStateChanged();
        }
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

            final String type = getPaymentMethod().getType();
            if (TextUtils.isEmpty(type)) {
                throw new CheckoutException("Payment method has empty or null type");
            }

            final AnalyticEvent analyticEvent = AnalyticEvent.create(context, flavor, type, getConfiguration().getShopperLocale());
            AnalyticsDispatcher.dispatchEvent(context, getConfiguration().getEnvironment(), analyticEvent);
        }
    }


    @Override
    public void observeOutputData(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<OutputDataT> observer) {
        // Parent component needs to overrides this for view to have access to the method in the package
        mOutputLiveData.observe(lifecycleOwner, observer);
    }

    @Nullable
    @Override
    public OutputDataT getOutputData() {
        return mOutputData;
    }

    /**
     * Called every time the {@link InputData} changes.
     *
     * @param inputData The new InputData
     * @return The OutputData after processing.
     */
    @NonNull
    protected abstract OutputDataT onInputDataChanged(@NonNull InputDataT inputData);

    @NonNull
    @WorkerThread
    protected abstract ComponentStateT createComponentState();

    protected void notifyException(@NonNull CheckoutException e) {
        Logger.e(TAG, "notifyException - " + e.getMessage());
        mComponentErrorLiveData.postValue(new ComponentError(e));
    }

    private void notifyStateChanged() {
        ThreadManager.EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                mPaymentComponentStateLiveData.postValue(createComponentState());
            }
        });
    }

    private void assertSupported(@NonNull PaymentMethod paymentMethod) {
        if (!isSupported(paymentMethod)) {
            throw new IllegalArgumentException("Unsupported payment method type " + paymentMethod);
        }
    }

    private boolean isSupported(@NonNull PaymentMethod paymentMethod) {
        for (String paymentMethodType : getSupportedPaymentMethodTypes()) {
            if (paymentMethodType.equals(paymentMethod.getType())) {
                return true;
            }
        }
        return false;
    }

    public void setCreatedForDropIn() {
        mIsCreatedForDropIn = true;
    }
}
