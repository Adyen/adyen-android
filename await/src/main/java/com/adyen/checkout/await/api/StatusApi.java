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
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.api.ThreadManager;
import com.adyen.checkout.core.exception.ApiCallException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

@SuppressWarnings("PMD.NullAssignment")
public final class StatusApi {
    private static final String TAG = LogUtil.getTag();

    //%1$s = client key
    private static final String STATUS_PATH = "services/PaymentInitiation/v1/status?token=%1$s";

    private static StatusApi sInstance;

    private final String mStatusUrlFormat;
    // We will only handle 1 call at a time.
    private StatusConnectionTask mCurrentTask;

    /**
     * Get the instance of the {@link StatusApi} for the specified environment.
     *
     * @param environment The URL of the server for making the calls. Should be the same used in the Payment.
     * @return The instance of the {@link StatusApi}.
     */
    @NonNull
    public static StatusApi getInstance(@NonNull Environment environment) {
        final String hostUrl = environment.getBaseUrl();
        synchronized (StatusApi.class) {
            if (sInstance == null || isDifferentHost(sInstance, hostUrl)) {
                sInstance = new StatusApi(hostUrl);
            }
            return sInstance;
        }
    }

    private static boolean isDifferentHost(@NonNull StatusApi statusApi, @NonNull String hostUrl) {
        return !statusApi.mStatusUrlFormat.startsWith(hostUrl);
    }

    private StatusApi(@NonNull String host) {
        Logger.v(TAG, "Environment URL - " + host);
        mStatusUrlFormat = host + STATUS_PATH;
    }

    void taskFinished() {
        synchronized (this) {
            mCurrentTask = null;
        }
    }

    /**
     * Starts a request to to the Status endpoing.
     *
     * @param clientKey The clientKey that identifies the merchant.
     * @param paymentData The paymentData that identifies the payment.
     * @param callback The callback to receive the result.
     */
    public void callStatus(@NonNull String clientKey, @NonNull String paymentData, @NonNull StatusConnectionTask.StatusCallback callback) {
        Logger.v(TAG, "getStatus");
        final String url = String.format(mStatusUrlFormat, clientKey);

        synchronized (this) {
            if (mCurrentTask != null) {
                Logger.e(TAG, "Status already pending.");
                callback.onFailed(new ApiCallException("Other Status call already pending."));
            }

            final StatusRequest statusRequest = new StatusRequest();
            statusRequest.setPaymentData(paymentData);
            mCurrentTask = new StatusConnectionTask(this, url, statusRequest, callback);
            ThreadManager.EXECUTOR.submit(mCurrentTask);
        }
    }
}
