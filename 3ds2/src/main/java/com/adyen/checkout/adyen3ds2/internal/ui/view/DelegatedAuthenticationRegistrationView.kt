/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 16/2/2023.
 */

package com.adyen.checkout.adyen3ds2.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.authentication.AdyenAuthentication
import com.adyen.authentication.AuthenticationResult
import com.adyen.checkout.adyen3ds2.R
import com.adyen.checkout.adyen3ds2.databinding.DelegatedAuthenticationRegistrationViewBinding
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.adyen3ds2.model.DelegatedAuthenticationRegistrationResult
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class DelegatedAuthenticationRegistrationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentView {

    private val binding: DelegatedAuthenticationRegistrationViewBinding =
        DelegatedAuthenticationRegistrationViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is Adyen3DS2Delegate) throw IllegalArgumentException("Unsupported delegate type")
        this.localizedContext = localizedContext

        val adyenAuthentication = delegate.getAdyenAuthentication()
        val sdkInput = delegate.getRegistrationSdkInput()
        when {
            adyenAuthentication == null -> {
                Logger.w(TAG, "Couldn't perform Delegated Authentication: adyenAuthentication is null")
                delegate.onRegistrationFailed()
            }
            sdkInput == null -> {
                Logger.w(TAG, "Couldn't perform Delegated Authentication: sdkInput is null")
                delegate.onRegistrationFailed()
            }
            else -> {
                setLocalizedStrings()
                collectTimerUpdates(coroutineScope, delegate)
                setOnClickListeners(coroutineScope, delegate, adyenAuthentication, sdkInput)
            }
        }
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit

    private fun setLocalizedStrings() {
        binding.textViewTitle.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_registration_title)
        binding.textViewDescription.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_registration_description)
        binding.buttonEnable.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_registration_positive_button)
        binding.buttonNotNow.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_registration_negative_button)
    }

    private fun collectTimerUpdates(coroutineScope: CoroutineScope, delegate: Adyen3DS2Delegate) {
        coroutineScope.launch {
            delegate.authenticationTimerFlow.collect {
                setTimerData(it)
                if (it.millisUntilFinished == 0L) {
                    delegate.onRegistrationResult(DelegatedAuthenticationRegistrationResult.Timeout)
                }
            }
        }
    }

    private fun setTimerData(timerData: TimerData) {
        val minutes = timerData.millisUntilFinished.milliseconds.inWholeMinutes
        val seconds = timerData.millisUntilFinished.milliseconds.inWholeSeconds % 1.minutes.inWholeSeconds

        val timeLeftString = localizedContext.getString(
            R.string.checkout_3ds2_time_left_to_approve,
            minutes,
            seconds
        )

        binding.textViewTimeLeft.text = timeLeftString
        binding.progressIndicator.progress = timerData.progress
    }

    private fun setOnClickListeners(
        coroutineScope: CoroutineScope,
        delegate: Adyen3DS2Delegate,
        adyenAuthentication: AdyenAuthentication,
        sdkInput: String
    ) {
        binding.buttonEnable.setOnClickListener {
            coroutineScope.launch {
                val registrationResult = adyenAuthentication.register(sdkInput)
                when (registrationResult) {
                    is AuthenticationResult.RegistrationSuccessful -> {
                        val result = DelegatedAuthenticationRegistrationResult.RegistrationSuccessful(
                            registrationResult.sdkOutput
                        )
                        delegate.onRegistrationResult(result)
                    }
                    is AuthenticationResult.AuthenticationError -> {
                        delegate.onRegistrationFailed()
                    }
                    is AuthenticationResult.Error -> {
                        delegate.onRegistrationFailed()
                    }
                    else -> {
                        delegate.onRegistrationResult(DelegatedAuthenticationRegistrationResult.SkippedByUser)
                    }
                }
            }
        }

        binding.buttonNotNow.setOnClickListener {
            delegate.onRegistrationResult(DelegatedAuthenticationRegistrationResult.SkippedByUser)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
