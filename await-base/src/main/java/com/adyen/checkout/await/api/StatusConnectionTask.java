/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */

package com.adyen.checkout.await.api;

import androidx.annotation.NonNull;

import com.adyen.checkout.await.model.StatusRequest;
import com.adyen.checkout.await.model.StatusResponse;
import com.adyen.checkout.core.api.ConnectionTask;
import com.adyen.checkout.core.api.ThreadManager;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exception.ApiCallException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("PMD.NullAssignment")
public class StatusConnectionTask extends ConnectionTask<StatusResponse> {
    private static final String TAG = LogUtil.getTag();

    private static final int SAFETY_TIMEOUT = 100;

    @SuppressWarnings(Lint.SYNTHETIC)
    StatusConnectionTask.StatusCallback mCallback;

    private final StatusApi mStatusApi;

    StatusConnectionTask(
            @NonNull StatusApi statusApi,
            @NonNull String logoUrl,
            @NonNull StatusRequest statusRequest,
            @NonNull StatusConnectionTask.StatusCallback callback) {
        super(new StatusConnection(logoUrl, statusRequest));
        mStatusApi = statusApi;
        mCallback = callback;
    }

    @Override
    protected void done() {
        Logger.v(TAG, "done");

        if (isCancelled()) {
            Logger.d(TAG, "canceled");
            notifyFailed(new ApiCallException("Execution canceled."));
        } else {
            try {
                // timeout just to make sure we don't get stuck, get call is blocking but should be finished or canceled by now.
                final StatusResponse result = get(SAFETY_TIMEOUT, TimeUnit.MILLISECONDS);
                notifySuccess(result);
            } catch (ExecutionException e) {
                Logger.e(TAG, "Execution failed.", e);
                notifyFailed(new ApiCallException("Execution failed.", e));
            } catch (InterruptedException e) {
                Logger.e(TAG, "Execution interrupted.", e);
                notifyFailed(new ApiCallException("Execution interrupted.", e));
            } catch (TimeoutException e) {
                Logger.e(TAG, "Execution timed out.", e);
                notifyFailed(new ApiCallException("Execution timed out.", e));
            }
        }
    }

    @SuppressWarnings(Lint.SYNTHETIC)
    StatusApi getApi() {
        return mStatusApi;
    }

    private void notifySuccess(@NonNull final StatusResponse statusResponse) {
        ThreadManager.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                getApi().taskFinished();
                mCallback.onSuccess(statusResponse);
                mCallback = null;
            }
        });
    }

    private void notifyFailed(@NonNull final ApiCallException exception) {
        ThreadManager.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                getApi().taskFinished();
                mCallback.onFailed(exception);
                mCallback = null;
            }
        });
    }

    /**
     * Interface to receive events on Status task completion.
     */
    public interface StatusCallback {

        /**
         * This method will be called on the Main Thread when the Status is received.
         *
         * @param statusResponse The requested status.
         */
        void onSuccess(@NonNull StatusResponse statusResponse);

        /**
         * This method will be called on the Main Thread if there was an error retrieving the Status.
         *
         * @param exception The reason why the call failed.
         */
        void onFailed(@NonNull ApiCallException exception);
    }
}
