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
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.ActionComponentData;
import com.adyen.checkout.components.ActionComponentProvider;
import com.adyen.checkout.components.ViewableComponent;
import com.adyen.checkout.components.base.BaseActionComponent;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.components.base.lifecycle.BaseLifecycleObserver;
import com.adyen.checkout.components.model.payments.response.Action;
import com.adyen.checkout.components.status.StatusRepository;
import com.adyen.checkout.components.status.api.StatusResponseUtils;
import com.adyen.checkout.components.status.model.StatusResponse;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class AwaitComponent extends BaseActionComponent<AwaitConfiguration>
        implements ViewableComponent<AwaitOutputData, AwaitConfiguration, ActionComponentData> {

    static final String TAG = LogUtil.getTag();

    private static final String PAYLOAD_DETAILS_KEY = "payload";

    public static final ActionComponentProvider<AwaitComponent, AwaitConfiguration> PROVIDER = new AwaitComponentProvider();

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

    public AwaitComponent(@NonNull SavedStateHandle savedStateHandle, @NonNull Application application, @NonNull AwaitConfiguration configuration) {
        super(savedStateHandle, application, configuration);
        mStatusRepository = StatusRepository.getInstance(configuration.getEnvironment());
    }

    @Override
    public boolean canHandleAction(@NonNull Action action) {
        return PROVIDER.canHandleAction(action);
    }

    @Override
    protected void handleActionInternal(@NonNull Activity activity, @NonNull Action action) throws ComponentException {
        final Configuration configuration = getConfiguration();
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
        // noop
    }


    void createOutputData(@Nullable StatusResponse statusResponse) {
        final boolean isValid = statusResponse != null && StatusResponseUtils.isFinalResult(statusResponse);
        final AwaitOutputData outputData = new AwaitOutputData(isValid, mPaymentMethodType);
        mOutputLiveData.setValue(outputData);
    }
}
