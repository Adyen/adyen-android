/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 5/12/2022.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import com.adyen.authentication.AdyenAuthentication
import com.adyen.authentication.AuthenticationLauncher
import com.adyen.checkout.components.bundle.SavedStateHandleContainer
import com.adyen.checkout.components.bundle.SavedStateHandleProperty
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("TooManyFunctions")
internal class DelegatedAuthentication(
    override val savedStateHandle: SavedStateHandle,
    @DrawableRes val merchantLogo: Int?
) : SavedStateHandleContainer {

    private var adyenAuthentication: AdyenAuthentication? = null

    private var pendingRegistrationSdkInput: String? by SavedStateHandleProperty(REGISTRATION_SDK_INPUT)
    private var pendingAuthenticationSdkInput: String? by SavedStateHandleProperty(AUTHENTICATION_SDK_INPUT)

    private var countDownTimer: CountDownTimer? = null
    internal var isTimerStarted: Boolean? by SavedStateHandleProperty(IS_TIMER_STARTED)
    private var currentMillisUntilFinished: Long? by SavedStateHandleProperty(MILLIS_UNTIL_FINISHED)

    private var _timeoutTimerFlow = MutableStateFlow<TimerData?>(null)
    internal var timeoutTimerFlow = _timeoutTimerFlow.filterNotNull()

    fun initialize(coroutineScope: CoroutineScope) {
        if (isTimerStarted == true) {
            startTimer(coroutineScope)
        }
    }

    fun initAdyenAuthentication(context: Context, authenticationLauncher: AuthenticationLauncher) {
        adyenAuthentication = AdyenAuthentication(context, authenticationLauncher)
    }

    fun getAdyenAuthentication(): AdyenAuthentication? = adyenAuthentication

    internal suspend fun hasCredential(
        application: Application,
        sdkInput: String
    ): Boolean? {
        val hasDACredential = try {
            AdyenAuthentication.hasCredential(application, sdkInput)
        } catch (e: ClassNotFoundException) {
            Logger.e(TAG, "hasCredential not executed because Authentication SDK is not present in project.")
            null
        } catch (e: NoClassDefFoundError) {
            Logger.e(TAG, "hasCredential not executed because Authentication SDK is not present in project.")
            null
        } catch (e: IncompatibleClassChangeError) {
            Logger.e(
                TAG,
                "hasCredential not executed because Authentication SDK version is incompatible with compiled version."
            )
            null
        }
        Logger.d(TAG, "hasDACredential: $hasDACredential")
        return hasDACredential
    }

    internal fun startTimer(coroutineScope: CoroutineScope) {
        isTimerStarted = true
        val millisUntilFinished = currentMillisUntilFinished ?: DEFAULT_TIMEOUT_IN_MILLIS
        publishTimerUpdate(millisUntilFinished)
        coroutineScope.launch(Dispatchers.Main) {
            countDownTimer = object : CountDownTimer(millisUntilFinished, 500.milliseconds.inWholeMilliseconds) {
                override fun onTick(millisUntilFinished: Long) = publishTimerUpdate(millisUntilFinished)

                override fun onFinish() = publishTimerUpdate(0)
            }
            countDownTimer?.start()
        }
    }

    internal fun initRegistration(sdkInput: String) {
        pendingRegistrationSdkInput = sdkInput
    }

    internal fun isRegistrationInitiated(): Boolean = pendingRegistrationSdkInput != null

    internal fun getRegistrationSdkInput(): String? = pendingRegistrationSdkInput

    internal fun completeRegistration() {
        resetToDefaults()
    }

    internal fun initAuthentication(sdkInput: String) {
        pendingAuthenticationSdkInput = sdkInput
    }

    internal fun isAuthenticationInitiated(): Boolean = pendingAuthenticationSdkInput != null

    internal fun getAuthenticationSdkInput(): String? = pendingAuthenticationSdkInput

    internal fun completeAuthentication() {
        resetToDefaults()
    }

    private fun resetToDefaults() {
        pendingRegistrationSdkInput = null
        pendingAuthenticationSdkInput = null
        countDownTimer?.cancel()
        countDownTimer = null
        isTimerStarted = false
        currentMillisUntilFinished = DEFAULT_TIMEOUT_IN_MILLIS
    }

    internal fun onCleared() {
        countDownTimer?.cancel()
        countDownTimer = null
        adyenAuthentication = null
    }

    private fun publishTimerUpdate(millisUntilFinished: Long) {
        currentMillisUntilFinished = millisUntilFinished
        val progressPercentage = (millisUntilFinished.toDouble() / DEFAULT_TIMEOUT_IN_MILLIS) * HUNDRED
        val progress = progressPercentage.toInt()
        val timerData = TimerData(millisUntilFinished, progress)
        _timeoutTimerFlow.tryEmit(timerData)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private val DEFAULT_TIMEOUT_IN_MILLIS = 90.seconds.inWholeMilliseconds
        private const val HUNDRED = 100

        private const val REGISTRATION_SDK_INPUT = "da_registration_sdk_input"
        private const val AUTHENTICATION_SDK_INPUT = "da_authentication_sdk_input"

        private const val MILLIS_UNTIL_FINISHED = "da_millis_until_finished"
        private const val IS_TIMER_STARTED = "da_is_timer_started"
    }
}
