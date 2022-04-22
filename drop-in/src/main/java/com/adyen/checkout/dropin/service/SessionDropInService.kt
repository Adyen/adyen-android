/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/4/2022.
 */

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/4/2022.
 */

package com.adyen.checkout.dropin.service

import android.content.Intent
import android.os.IBinder
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.repository.SessionRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

internal class SessionDropInService : DropInService() {

    private var isInitialized = false

    private lateinit var sessionRepository: SessionRepository

    override fun onBind(intent: Intent?): IBinder {
        if (
            !isInitialized &&
            intent?.hasExtra(INTENT_EXTRA_CONFIGURATION) == true &&
            intent.hasExtra(INTENT_EXTRA_SESSION)
        ) {
            val configuration = requireNotNull(intent.getParcelableExtra<Configuration>(INTENT_EXTRA_CONFIGURATION))
            sessionRepository = SessionRepository(
                configuration = configuration,
                session = requireNotNull(intent.getParcelableExtra(INTENT_EXTRA_SESSION)),
            )

            setupSession()

            isInitialized = true
        }

        return super.onBind(intent)
    }

    private fun setupSession() {
        launch {
            sessionRepository.setupSession(null)
                .fold(
                    onSuccess = {
                        sendSessionSetupResult(SessionSetupDropInServiceResult.Success(it))
                    },
                    onFailure = {
                        val result = SessionSetupDropInServiceResult.Error(
                            errorMessage = "Something went wrong while setting up the session",
                            reason = it.message,
                            dismissDropIn = false,
                        )
                        sendSessionSetupResult(result)
                    }
                )
        }
    }

    private fun sendSessionSetupResult(sessionSetupDropInServiceResult: SessionSetupDropInServiceResult) {
        Logger.d(TAG, "Sending session setup result")
        emitResult(sessionSetupDropInServiceResult)
    }

    override fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        launch {
            sessionRepository.submitPayment(paymentComponentState.data)
                .fold(
                    onSuccess = {
                        // TODO handle response correctly
                        sendResult(DropInServiceResult.Finished("Success!"))
                    },
                    onFailure = {
                        val result = DropInServiceResult.Error(
                            errorMessage = "Something went wrong while setting up the session",
                            reason = it.message,
                            dismissDropIn = false,
                        )
                        sendResult(result)
                    }
                )
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val INTENT_EXTRA_CONFIGURATION = "INTENT_EXTRA_CONFIGURATION"
        private const val INTENT_EXTRA_SESSION = "INTENT_EXTRA_SESSION"
    }
}
