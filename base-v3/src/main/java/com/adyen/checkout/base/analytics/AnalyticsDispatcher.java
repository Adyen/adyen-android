/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/6/2019.
 */

package com.adyen.checkout.base.analytics;

import static com.adyen.checkout.core.api.SSLSocketUtil.TLS_SOCKET_FACTORY;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

// TODO change to try-with-resources after updating min API lvl
@SuppressWarnings("PMD.CloseResource")
public class AnalyticsDispatcher extends JobIntentService {
    private static final String TAG = LogUtil.getTag();

    private static final String EVENT_KEY = "analytic_event";
    private static final String ENV_URL_KEY = "env_url_key";
    private static final String ANALYTICS_ENDPOINT = "images/analytics.png";

    private static final int ANALYTICS_JOB_ID = 4747;

    /**
     * Request to dispatch a new event.
     *
     * @param context     A context to send the intent.
     * @param environment The Environment to use for sending the events to.
     * @param event       The Event to be sent.
     */
    public static void dispatchEvent(@NonNull Context context, @NonNull Environment environment, @NonNull AnalyticEvent event) {
        final Intent workIntent = new Intent();
        workIntent.putExtra(EVENT_KEY, event);
        workIntent.putExtra(ENV_URL_KEY, environment.getBaseUrl());
        AnalyticsDispatcher.enqueueWork(context, AnalyticsDispatcher.class, ANALYTICS_JOB_ID, workIntent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final AnalyticEvent event = intent.getParcelableExtra(EVENT_KEY);
        final String envUrl = intent.getStringExtra(ENV_URL_KEY);
        if (event == null) {
            Logger.e(TAG, "Analytics event is null.");
            return;
        }

        if (envUrl == null) {
            Logger.e(TAG, "env url is null.");
            return;
        }

        Logger.v(TAG, "Sending analytic event.");

        HttpsURLConnection urlConnection = null;
        try {
            final URL finalUrl = event.toUrl(envUrl + ANALYTICS_ENDPOINT);
            urlConnection = (HttpsURLConnection) finalUrl.openConnection();
            urlConnection.setSSLSocketFactory(TLS_SOCKET_FACTORY);
            urlConnection.connect();
            final InputStream inputStream = urlConnection.getInputStream();
            // Need to read to consume the inputStream for the connection to count on the backend.
            //noinspection ResultOfMethodCallIgnored
            inputStream.read();
            inputStream.close();
        } catch (IOException e) {
            Logger.e(TAG, "Failed to send analytics event.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
