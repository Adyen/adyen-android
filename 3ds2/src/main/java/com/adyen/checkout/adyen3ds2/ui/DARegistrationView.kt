package com.adyen.checkout.adyen3ds2.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.authentication.AuthenticationResult
import com.adyen.checkout.adyen3ds2.Adyen3DS2Delegate
import com.adyen.checkout.adyen3ds2.R
import com.adyen.checkout.adyen3ds2.databinding.ViewDaRegistrationBinding
import com.adyen.checkout.adyen3ds2.model.DARegistrationResult
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("TooManyFunctions")
internal class DARegistrationView @JvmOverloads constructor(
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

    private val binding: ViewDaRegistrationBinding =
        ViewDaRegistrationBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is Adyen3DS2Delegate) throw IllegalArgumentException()
        this.localizedContext = localizedContext

        val adyenAuthentication = delegate.getAdyenAuthentication()
        if (adyenAuthentication == null) {
            Logger.w(TAG, "Couldn't perform DA registration flow: adyenAuthentication is null")
            delegate.onRegistrationFailed()
            return
        }
        val sdkInput = delegate.getRegistrationSdkInput()
        if (sdkInput == null) {
            Logger.w(TAG, "Couldn't perform DA registration flow: sdkInput is null")
            delegate.onRegistrationFailed()
            return
        }

        binding.tvTitle.text = localizedContext.getString(R.string.checkout_3ds2_da_registration_title)
        binding.tvDescription.text = localizedContext.getString(R.string.checkout_3ds2_da_registration_description)
        binding.btnEnable.text = localizedContext.getString(R.string.checkout_3ds2_da_registration_positive_button)
        binding.btnNotNow.text = localizedContext.getString(R.string.checkout_3ds2_da_registration_negative_button)

        binding.btnEnable.setOnClickListener {
            coroutineScope.launch {
                when (val registrationResult = adyenAuthentication.register(sdkInput)) {
                    is AuthenticationResult.RegistrationSuccessful -> {
                        val result = DARegistrationResult.RegistrationSuccessful(registrationResult.sdkOutput)
                        delegate.onRegistrationResult(result)
                    }
                    is AuthenticationResult.AuthenticationError -> {
                        delegate.onRegistrationFailed()
                    }
                    is AuthenticationResult.Error -> {
                        delegate.onRegistrationFailed()
                    }
                    else -> {
                        delegate.onRegistrationResult(DARegistrationResult.NotNow)
                    }
                }
            }
        }

        binding.btnNotNow.setOnClickListener {
            delegate.onRegistrationResult(DARegistrationResult.NotNow)
        }

        coroutineScope.launch {
            delegate.getAuthenticationTimerFlow().collect {
                withContext(Dispatchers.Main) {
                    setTimerData(it)
                }
                if (it.millisUntilFinished == 0L) {
                    delegate.onRegistrationResult(DARegistrationResult.Timeout)
                }
            }
        }
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit

    private fun setTimerData(timerData: TimerData) {
        val minutes = timerData.millisUntilFinished.milliseconds.inWholeMinutes
        val seconds = timerData.millisUntilFinished.milliseconds.inWholeSeconds % 1.minutes.inWholeSeconds

        val timeLeftString = localizedContext.getString(
            R.string.checkout_3ds2_time_left_to_approve,
            minutes,
            seconds
        )

        binding.tvTimeLeft.text = timeLeftString
        binding.lpiProgress.progress = timerData.progress
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
