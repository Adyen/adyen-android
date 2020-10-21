/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */

package com.adyen.checkout.await;

import android.app.Activity;
import android.app.Application;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.await.api.StatusResponseUtils;
import com.adyen.checkout.await.model.StatusResponse;
import com.adyen.checkout.base.ActionComponentData;
import com.adyen.checkout.base.ActionComponentProvider;
import com.adyen.checkout.base.ViewableComponent;
import com.adyen.checkout.base.component.ActionComponentProviderImpl;
import com.adyen.checkout.base.component.BaseActionComponent;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.lifecycle.BaseLifecycleObserver;
import com.adyen.checkout.base.model.payments.response.Action;
import com.adyen.checkout.base.model.payments.response.AwaitAction;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AwaitComponent extends BaseActionComponent<AwaitConfiguration>
        implements ViewableComponent<AwaitOutputData, AwaitConfiguration, ActionComponentData> {
    @SuppressWarnings(Lint.SYNTHETIC)
    static final String TAG = LogUtil.getTag();

    private static final String PAYLOAD_DETAILS_KEY = "payload";

    public static final ActionComponentProvider<AwaitComponent> PROVIDER
            = new ActionComponentProviderImpl<>(AwaitComponent.class, AwaitConfiguration.class, true);

    @SuppressWarnings(Lint.SYNTHETIC)
    final StatusRepository mStatusRepository;

    private final MutableLiveData<AwaitOutputData> mOutputLiveData = new MutableLiveData<>();
    private String mPaymentMethodType;

    private final Observer<StatusResponse> mResponseObserver = new Observer<StatusResponse>() {
        @Override
        public void onChanged(@Nullable StatusResponse statusResponse) {
            Logger.v(TAG, "onChanged - " + (statusResponse == null ? "null" : statusResponse.getResultCode()));
            createOutputData(statusResponse);
            if (statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse)) {
                onPollingSuccessful(statusResponse);
            }
        }
    };
    private final Observer<ComponentException> mErrorObserver = new Observer<ComponentException>() {
        @Override
        public void onChanged(@Nullable ComponentException e) {
            // StatusRepository will post null errors to reset it's status. We can ignore.
            if (e != null) {
                Logger.e(TAG, "onError");
                notifyException(e);
            }
        }
    };

    public AwaitComponent(@NonNull Application application, @NonNull AwaitConfiguration configuration) {
        super(application, configuration);
        mStatusRepository = StatusRepository.getInstance(configuration.getEnvironment());
    }

    @NonNull
    @Override
    protected List<String> getSupportedActionTypes() {
        final String[] supportedCodes = {AwaitAction.ACTION_TYPE};
        return Collections.unmodifiableList(Arrays.asList(supportedCodes));
    }

    @Override
    protected void handleActionInternal(@NonNull Activity activity, @NonNull Action action) throws ComponentException {
        final Configuration configuration = getConfiguration();
        if (configuration == null) {
            throw new ComponentException("Configuration not found");
        }
        mPaymentMethodType = action.getPaymentMethodType();
        // Notify UI to get the logo.
        createOutputData(null);
        mStatusRepository.startPolling(configuration.getClientKey(), getPaymentData());
    }

    @Override
    public void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ActionComponentData> observer) {
        super.observe(lifecycleOwner, observer);
        mStatusRepository.getStatusResponseLiveData().observe(lifecycleOwner, mResponseObserver);
        mStatusRepository.getErrorLiveData().observe(lifecycleOwner, mErrorObserver);

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.getLifecycle().addObserver(new BaseLifecycleObserver() {
            @Override
            public void onResume() {
                mStatusRepository.updateStatus();
            }
        });
    }

    @SuppressWarnings(Lint.SYNTHETIC)
    void onPollingSuccessful(@NonNull StatusResponse statusResponse) {
        // Not authorized status should still call /details so that merchant can get more info
        if (StatusResponseUtils.isFinalResult(statusResponse) && !TextUtils.isEmpty(statusResponse.getPayload())) {
            //noinspection ConstantConditions
            notifyDetails(createDetail(statusResponse.getPayload()));
        } else {
            notifyException(new ComponentException("Payment was not completed. - " + statusResponse.getResultCode()));
        }
    }

    private JSONObject createDetail(@NonNull String payload) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PAYLOAD_DETAILS_KEY, payload);
        } catch (JSONException e) {
            notifyException(new ComponentException("Failed to create details.", e));
        }
        return jsonObject;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.d(TAG, "onCleared");
        mStatusRepository.stopPolling();
    }

    @Override
    public void observeOutputData(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<AwaitOutputData> observer) {
        mOutputLiveData.observe(lifecycleOwner, observer);
    }

    @Nullable
    @Override
    public AwaitOutputData getOutputData() {
        return mOutputLiveData.getValue();
    }

    @Override
    public void sendAnalyticsEvent(@NonNull Context context) {
        // TODO: 28/08/2020 Do we have an event for this?
    }

    @SuppressWarnings(Lint.SYNTHETIC)
    void createOutputData(@Nullable StatusResponse statusResponse) {
        final boolean isValid = statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse);
        final AwaitOutputData outputData = new AwaitOutputData(isValid, mPaymentMethodType);
        mOutputLiveData.setValue(outputData);
    }
}
