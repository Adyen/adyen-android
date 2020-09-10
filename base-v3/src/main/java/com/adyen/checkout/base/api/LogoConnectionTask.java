/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/3/2019.
 */

package com.adyen.checkout.base.api;

import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.api.ConnectionTask;
import com.adyen.checkout.core.api.ThreadManager;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Task that wraps a Connection to get a Logo.
 */
// We are assigning null to mCallback to avoid memory leaks.
@SuppressWarnings("PMD.NullAssignment")
public final class LogoConnectionTask extends ConnectionTask<BitmapDrawable> {
    private static final String TAG = LogUtil.getTag();

    private static final int SAFETY_TIMEOUT = 100;

    @SuppressWarnings(Lint.SYNTHETIC)
    LogoCallback mCallback;
    private final String mLogoUrl;
    private final LogoApi mLogoApi;

    LogoConnectionTask(@NonNull LogoApi logoApi, @NonNull String logoUrl, @NonNull LogoCallback callback) {
        super(new LogoConnection(logoUrl));
        mLogoApi = logoApi;
        mLogoUrl = logoUrl;
        mCallback = callback;
    }

    @Override
    protected void done() {
        Logger.v(TAG, "done");

        if (isCancelled()) {
            Logger.d(TAG, "canceled");
            notifyFailed();
        } else {
            try {
                // timeout just to make sure we don't get stuck, get call is blocking but should be finished or canceled by now.
                final BitmapDrawable result = get(SAFETY_TIMEOUT, TimeUnit.MILLISECONDS);
                notifyLogo(result);
            } catch (ExecutionException e) {
                Logger.e(TAG, "Execution failed for logo  - " + getLogoUrl());
                notifyFailed();
            } catch (InterruptedException e) {
                Logger.e(TAG, "Execution interrupted.", e);
                notifyFailed();
            } catch (TimeoutException e) {
                Logger.e(TAG, "Execution timed out.", e);
                notifyFailed();
            }
        }
    }

    @SuppressWarnings(Lint.SYNTHETIC)
    String getLogoUrl() {
        return mLogoUrl;
    }

    @SuppressWarnings(Lint.SYNTHETIC)
    LogoApi getLogoApi() {
        return mLogoApi;
    }

    private void notifyLogo(@NonNull final BitmapDrawable drawable) {
        ThreadManager.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                getLogoApi().taskFinished(getLogoUrl(), drawable);
                mCallback.onLogoReceived(drawable);
                mCallback = null;
            }
        });
    }

    private void notifyFailed() {
        ThreadManager.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                getLogoApi().taskFinished(getLogoUrl(), null);
                mCallback.onReceiveFailed();
                mCallback = null;
            }
        });
    }

    /**
     * Interface to receive events on logo task completion.
     */
    public interface LogoCallback {

        /**
         * This method will be called on the Main Thread when the logo is received.
         *
         * @param drawable The requested logo, or an instance of Placeholder logo if the request failed.
         */
        void onLogoReceived(@NonNull BitmapDrawable drawable);

        /**
         * This method will be called on the Main Thread if there was an error retrieving the logo.
         */
        void onReceiveFailed();
    }
}
