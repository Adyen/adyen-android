package com.adyen.checkout.adyen3ds2.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.adyen.authentication.AdyenAuthentication
import com.adyen.authentication.AuthenticationResult
import com.adyen.checkout.adyen3ds2.Adyen3DS2Delegate
import com.adyen.checkout.adyen3ds2.R
import com.adyen.checkout.adyen3ds2.databinding.DelegatedAuthenticationViewBinding
import com.adyen.checkout.adyen3ds2.model.DelegatedAuthenticationResult
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class DelegatedAuthenticationView @JvmOverloads constructor(
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

    private val binding: DelegatedAuthenticationViewBinding =
        DelegatedAuthenticationViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is Adyen3DS2Delegate) throw IllegalArgumentException("Unsupported delegate type")
        this.localizedContext = localizedContext

        val adyenAuthentication = delegate.getAdyenAuthentication()
        val sdkInput = delegate.getAuthenticationSdkInput()
        val activity = getActivity(context) as? FragmentActivity
        when {
            adyenAuthentication == null -> {
                Logger.w(TAG, "Couldn't perform Delegated Authentication: adyenAuthentication is null")
                delegate.onAuthenticationFailed(activity)
            }
            sdkInput == null -> {
                Logger.w(TAG, "Couldn't perform Delegated Authentication: sdkInput is null")
                delegate.onAuthenticationFailed(activity)
            }
            activity == null -> {
                Logger.w(
                    TAG,
                    "Couldn't perform Delegated Authentication: activity is not FragmentActivity or null"
                )
                delegate.onAuthenticationFailed(activity)
            }
            else -> {
                setMerchantLogoIfProvided(delegate)
                setLocalizedStrings()
                collectTimerUpdates(activity, coroutineScope, delegate)
                setOnClickListeners(activity, coroutineScope, delegate, adyenAuthentication, sdkInput)
            }
        }
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit

    private fun setMerchantLogoIfProvided(delegate: Adyen3DS2Delegate) {
        delegate.getMerchantLogo()?.let {
            binding.imageViewLogo.setImageResource(it)
        }
    }

    private fun setLocalizedStrings() {
        binding.textViewTitle.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_title)
        binding.textViewDescription.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_description)
        binding.buttonAuthorise.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_positive_button)
        binding.buttonApproveDifferently.text =
            localizedContext.getString(R.string.checkout_3ds2_delegated_authentication_negative_button)

        binding.textViewRemoveCredentials.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_DelegatedAuthentication_OptOutTextView,
            localizedContext,
            formatHyperLink = true
        )
    }

    private fun collectTimerUpdates(
        activity: Activity,
        coroutineScope: CoroutineScope,
        delegate: Adyen3DS2Delegate
    ) {
        coroutineScope.launch {
            delegate.authenticationTimerFlow.collect {
                setTimerData(it)
                if (it.millisUntilFinished == 0L) {
                    delegate.onAuthenticationResult(DelegatedAuthenticationResult.Timeout, activity)
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
        activity: Activity,
        coroutineScope: CoroutineScope,
        delegate: Adyen3DS2Delegate,
        adyenAuthentication: AdyenAuthentication,
        sdkInput: String
    ) {
        binding.buttonAuthorise.setOnClickListener {
            coroutineScope.launch {
                val authenticationResult = adyenAuthentication.authenticate(sdkInput)
                when (authenticationResult) {
                    is AuthenticationResult.AuthenticationSuccessful -> {
                        val result = DelegatedAuthenticationResult.AuthenticationSuccessful(
                            authenticationResult.sdkOutput
                        )
                        delegate.onAuthenticationResult(result, activity)
                    }
                    is AuthenticationResult.AuthenticationError -> {
                        delegate.onAuthenticationFailed(activity)
                    }
                    is AuthenticationResult.Error -> {
                        delegate.onAuthenticationFailed(activity)
                    }
                    else -> {
                        delegate.onAuthenticationResult(DelegatedAuthenticationResult.SkippedByUser, activity)
                    }
                }
            }
        }

        binding.buttonApproveDifferently.setOnClickListener {
            delegate.onAuthenticationResult(DelegatedAuthenticationResult.SkippedByUser, activity)
        }

        binding.textViewRemoveCredentials.setOnClickListener {
            showRemoveCredentialsDialog(activity) {
                delegate.onAuthenticationResult(
                    DelegatedAuthenticationResult.RemoveCredentials,
                    activity
                )
            }
        }
    }

    private fun showRemoveCredentialsDialog(context: Context, onPositiveClick: () -> Unit) {
        val title = localizedContext.getString(
            R.string.checkout_3ds2_delegated_authentication_remove_credentials_title
        )
        val message = localizedContext.getString(
            R.string.checkout_3ds2_delegated_authentication_remove_credentials_description
        )
        val positiveButtonText = localizedContext.getString(
            R.string.checkout_3ds2_delegated_authentication_remove_credentials_positive_button
        )
        val negativeButtonText = localizedContext.getString(
            R.string.checkout_3ds2_delegated_authentication_remove_credentials_negative_button
        )
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick.invoke()
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
