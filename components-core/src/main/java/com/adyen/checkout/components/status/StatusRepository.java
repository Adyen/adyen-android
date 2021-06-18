/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */

package com.adyen.checkout.components.status;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.adyen.checkout.components.status.api.StatusApi;
import com.adyen.checkout.components.status.api.StatusConnectionTask;
import com.adyen.checkout.components.status.api.StatusResponseUtils;
import com.adyen.checkout.components.status.model.StatusResponse;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.exception.ApiCallException;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.concurrent.TimeUnit;

public final class StatusRepository {
    static final String TAG = LogUtil.getTag();

    private static final long POLLING_DELAY_FAST = TimeUnit.SECONDS.toMillis(2);
    private static final long POLLING_DELAY_SLOW = TimeUnit.SECONDS.toMillis(10);
    private static final long POLLING_THRESHOLD = TimeUnit.SECONDS.toMillis(60);
    private static final long POLLING_MAX_COUNT = TimeUnit.MINUTES.toMillis(15);

    private static StatusRepository sInstance;

    final Handler mHandler = new Handler();

    final Runnable mStatusPollingRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "mStatusPollingRunnable.run()");
            mStatusApi.callStatus(mClientKey, mPaymentData, mStatusCallback);
            updatePollingDelay();
            mHandler.postDelayed(mStatusPollingRunnable, mPollingDelay);
        }
    };

    final StatusApi mStatusApi;

    final MutableLiveData<StatusResponse> mStatusResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<ComponentException> mStatusErrorLiveData = new MutableLiveData<>();

    final StatusConnectionTask.StatusCallback mStatusCallback = new StatusConnectionTask.StatusCallback() {
        @Override
        public void onSuccess(@NonNull StatusResponse statusResponse) {
            Logger.d(TAG, "onSuccess - " + statusResponse.getResultCode());
            mStatusResponseLiveData.postValue(statusResponse);
            if (StatusResponseUtils.isFinalResult(statusResponse)) {
                stopPolling();
            }
        }

        @Override
        public void onFailed(@NonNull ApiCallException exception) {
            Logger.e(TAG, "onFailed");
            // TODO: 08/09/2020 check error type, fail flow if no internet?
        }
    };

    String mClientKey;
    String mPaymentData;
    Boolean mIsPolling = false;

    long mPollingDelay;
    private long mPollingStartTime;

    @NonNull
    public static StatusRepository getInstance(@NonNull Environment environment) {
        synchronized (StatusRepository.class) {
            if (sInstance == null) {
                sInstance = new StatusRepository(environment);
            }
        }
        return sInstance;
    }

    // private constructor
    private StatusRepository(@NonNull Environment environment) {
        mStatusApi = StatusApi.getInstance(environment);
    }

    @NonNull
    public LiveData<StatusResponse> getStatusResponseLiveData() {
        return mStatusResponseLiveData;
    }

    @NonNull
    public LiveData<ComponentException> getErrorLiveData() {
        return mStatusErrorLiveData;
    }

    /**
     * Start polling status requests for the provided payment.
     *
     * @param clientKey The client key that identifies the merchant.
     * @param paymentData The payment data of the payment we are requesting.
     */
    public void startPolling(@NonNull String clientKey, @NonNull String paymentData) {
        Logger.d(TAG, "startPolling");
        if (mIsPolling && clientKey.equals(mClientKey) && paymentData.equals(mPaymentData)) {
            Logger.e(TAG, "Already polling for this payment.");
            return;
        }
        stopPolling();
        mIsPolling = true;
        mClientKey = clientKey;
        mPaymentData = paymentData;
        mPollingStartTime = System.currentTimeMillis();

        mHandler.post(mStatusPollingRunnable);
    }

    /**
     * Immediately request a status update instead of waiting for the next poll result.
     */
    public void updateStatus() {
        Logger.d(TAG, "updateStatus");
        if (!mIsPolling) {
            Logger.d(TAG, "No polling in progress");
            return;
        }
        mHandler.removeCallbacks(mStatusPollingRunnable);
        mHandler.post(mStatusPollingRunnable);
    }

    /**
     * Stops the polling process.
     */
    public void stopPolling() {
        Logger.d(TAG, "stopPolling");
        if (!mIsPolling) {
            Logger.w(TAG, "Stop called with no polling in progress, stopping anyway");
        }
        mIsPolling = false;
        mHandler.removeCallbacksAndMessages(null);
        // Set null so that new observers don't get the status from the previous result
        // This could be replaced by other types of observable like Kotlin Flow
        mStatusResponseLiveData.setValue(null);
        mStatusErrorLiveData.setValue(null);
    }

    void updatePollingDelay() {
        final long elapsedTime = System.currentTimeMillis() - mPollingStartTime;
        if (elapsedTime <= POLLING_THRESHOLD) {
            mPollingDelay = POLLING_DELAY_FAST;
        } else if (elapsedTime <= POLLING_MAX_COUNT) {
            mPollingDelay = POLLING_DELAY_SLOW;
        } else {
            mStatusErrorLiveData.setValue(new ComponentException("Status requesting timed out with no result"));
        }
    }

    public long getMaxPollingDurationMillis() {
        return POLLING_MAX_COUNT;
    }
}
