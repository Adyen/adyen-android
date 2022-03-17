/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/6/2019.
 */
package com.adyen.checkout.components.analytics

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.SSLSocketUtil.TLS_SOCKET_FACTORY
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.HttpsURLConnection

class AnalyticsDispatcher : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        val event: AnalyticEvent? = intent.getParcelableExtra(EVENT_KEY)
        val envUrl = intent.getStringExtra(ENV_URL_KEY)
        if (event == null) {
            Logger.e(TAG, "Analytics event is null.")
            return
        }
        if (envUrl == null) {
            Logger.e(TAG, "env url is null.")
            return
        }
        Logger.v(TAG, "Sending analytic event.")
        var urlConnection: HttpsURLConnection? = null
        try {
            val finalUrl = event.toUrl(envUrl + ANALYTICS_ENDPOINT)
            urlConnection = finalUrl.openConnection() as HttpsURLConnection
            urlConnection.sslSocketFactory = TLS_SOCKET_FACTORY
            urlConnection.connect()
            urlConnection.inputStream.use { inputStream ->
                // Need to read to consume the inputStream for the connection to count on the backend.
                inputStream.read()
            }
            // NoSuchAlgorithmException and KeyManagementException are from getTLS_SOCKET_FACTORY
        } catch (e: IOException) {
            Logger.e(TAG, "Failed to send analytics event.", e)
        } catch (e: NoSuchAlgorithmException) {
            Logger.e(TAG, "Failed to send analytics event.", e)
        } catch (e: KeyManagementException) {
            Logger.e(TAG, "Failed to send analytics event.", e)
        } finally {
            urlConnection?.disconnect()
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val EVENT_KEY = "analytic_event"
        private const val ENV_URL_KEY = "env_url_key"
        private const val ANALYTICS_ENDPOINT = "images/analytics.png"
        private const val ANALYTICS_JOB_ID = 4747

        /**
         * Request to dispatch a new event.
         *
         * @param context     A context to send the intent.
         * @param environment The Environment to use for sending the events to.
         * @param event       The Event to be sent.
         */
        @JvmStatic
        fun dispatchEvent(context: Context, environment: Environment, event: AnalyticEvent) {
            val workIntent = Intent()
            workIntent.putExtra(EVENT_KEY, event)
            workIntent.putExtra(ENV_URL_KEY, environment.baseUrl)
            enqueueWork(context, AnalyticsDispatcher::class.java, ANALYTICS_JOB_ID, workIntent)
        }
    }
}
