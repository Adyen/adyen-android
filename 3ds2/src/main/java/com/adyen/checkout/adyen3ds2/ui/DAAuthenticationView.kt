package com.adyen.checkout.adyen3ds2.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.adyen.authentication.AuthenticationResult
import com.adyen.checkout.adyen3ds2.Adyen3DS2Delegate
import com.adyen.checkout.adyen3ds2.R
import com.adyen.checkout.adyen3ds2.databinding.ViewDaAuthenticationBinding
import com.adyen.checkout.adyen3ds2.model.DAAuthenticationResult
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class DAAuthenticationView @JvmOverloads constructor(
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

    private val binding: ViewDaAuthenticationBinding =
        ViewDaAuthenticationBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is Adyen3DS2Delegate) throw IllegalArgumentException()
        this.localizedContext = localizedContext

        val adyenAuthentication = delegate.getAdyenAuthentication()
        if (adyenAuthentication == null) {
            Logger.w(TAG, "Couldn't perform DA authentication flow: adyenAuthentication is null")
            delegate.onAuthenticationFailed(getActivity(context))
            return
        }
        val sdkInput = delegate.getAuthenticationSdkInput()
        if (sdkInput == null) {
            Logger.w(TAG, "Couldn't perform DA authentication: sdkInput is null")
            delegate.onAuthenticationFailed(getActivity(context))
            return
        }
        val activity = getActivity(context) as? FragmentActivity
        if (activity == null) {
            Logger.w(TAG, "Couldn't perform DA authentication flow: activity is not FragmentActivity or null")
            delegate.onAuthenticationFailed(activity)
            return
        }

        binding.tvTitle.text = localizedContext.getString(R.string.checkout_3ds2_da_authentication_title)
        binding.tvDescription.text = localizedContext.getString(R.string.checkout_3ds2_da_authentication_description)
        binding.btnAuthorise.text = localizedContext.getString(R.string.checkout_3ds2_da_authentication_positive_button)
        binding.btnAuthoriseDifferently.text = localizedContext.getString(
            R.string.checkout_3ds2_da_authentication_negative_button
        )
        binding.tvRemoveCredentials.text = localizedContext.getString(
            R.string.checkout_3ds2_da_authentication_remove_credentials
        )

        binding.btnAuthorise.setOnClickListener {
            coroutineScope.launch {
                when (val authenticationResult = adyenAuthentication.authenticate(sdkInput)) {
                    is AuthenticationResult.AuthenticationSuccessful -> {
                        val result = DAAuthenticationResult.AuthenticationSuccessful(authenticationResult.sdkOutput)
                        delegate.onAuthenticationResult(result, activity)
                    }
                    is AuthenticationResult.AuthenticationError -> {
                        delegate.onAuthenticationFailed(activity)
                    }
                    is AuthenticationResult.Error -> {
                        delegate.onAuthenticationFailed(activity)
                    }
                    else -> {
                        delegate.onAuthenticationResult(DAAuthenticationResult.NotNow, activity)
                    }
                }
            }
        }

        binding.btnAuthoriseDifferently.setOnClickListener {
            delegate.onAuthenticationResult(DAAuthenticationResult.NotNow, activity)
        }

        applyTextSpans()
        binding.tvRemoveCredentials.setOnClickListener {
            showRemoveCredentialsDialog(activity) {
                delegate.onAuthenticationResult(DAAuthenticationResult.RemoveCredentials, activity)
            }
        }

        coroutineScope.launch {
            delegate.getAuthenticationTimerFlow().collect {
                withContext(Dispatchers.Main) {
                    setTimerData(it)
                }
                if (it.millisUntilFinished == 0L) {
                    delegate.onAuthenticationResult(DAAuthenticationResult.Timeout, activity)
                }
            }
        }
    }

    override fun getView(): View = this

    override fun highlightValidationErrors() = Unit

    private fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

    private fun applyTextSpans() {
        val removeCredentialsText = localizedContext.getText(
            R.string.checkout_3ds2_da_authentication_remove_credentials
        ) as SpannedString

        val removeCredentialsStringBuilder = SpannableStringBuilder(removeCredentialsText)
        val annotations = removeCredentialsText.getSpans(
            0,
            removeCredentialsText.length,
            android.text.Annotation::class.java
        )
        annotations?.indices?.forEach { i ->
            val annotation = annotations[i]
            val annotationKey = annotation.key
            if (annotationKey.equals("link")) {
                val linkTextColor = ContextCompat.getColor(localizedContext, R.color.textColorLink)
                removeCredentialsStringBuilder.setSpan(
                    ForegroundColorSpan(linkTextColor),
                    removeCredentialsText.getSpanStart(annotation),
                    removeCredentialsText.getSpanEnd(annotation),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        binding.tvRemoveCredentials.text = removeCredentialsStringBuilder
    }

    private fun showRemoveCredentialsDialog(context: Context, onPositiveClick: () -> Unit) {
        val onClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> onPositiveClick.invoke()
            }
            dialog.dismiss()
        }
        MaterialAlertDialogBuilder(context, R.style.DAMaterialAlertDialogTheme)
            .setTitle(localizedContext.getString(R.string.checkout_3ds2_da_remove_credentials_title))
            .setMessage(localizedContext.getString(R.string.checkout_3ds2_da_remove_credentials_description))
            .setPositiveButton(
                localizedContext.getString(R.string.checkout_3ds2_da_remove_credentials_positive_button),
                onClickListener
            )
            .setNegativeButton(
                localizedContext.getString(R.string.checkout_3ds2_da_remove_credentials_negative_button),
                onClickListener
            )
            .create()
            .show()
    }

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
