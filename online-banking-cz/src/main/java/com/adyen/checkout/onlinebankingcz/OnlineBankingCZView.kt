/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.ui.adapter.SimpleTextListAdapter
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.onlinebankingcz.databinding.OnlineBankingCzSpinnerLayoutBinding

class OnlineBankingCZView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<
        OnlineBankingOutputData,
        OnlineBankingConfiguration,
        PaymentComponentState<OnlineBankingCZPaymentMethod>,
        OnlineBankingCZComponent
        >(context, attrs, defStyleAttr),
    AdapterView.OnItemSelectedListener {

    private val binding: OnlineBankingCzSpinnerLayoutBinding =
        OnlineBankingCzSpinnerLayoutBinding.inflate(LayoutInflater.from(context), this)

    private val issuersAdapter: SimpleTextListAdapter<OnlineBankingModel> = SimpleTextListAdapter(context)

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    override fun onComponentAttached() {
        issuersAdapter.setItems(component.issuers)
    }

    override fun initView() {
        binding.autoCompleteTextViewIssuers.apply {
            inputType = 0
            setAdapter(issuersAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                Logger.d(TAG, "onItemSelected - " + issuersAdapter.getItem(position).name)
                component.inputData.selectedIssuer = issuersAdapter.getItem(position)
                component.notifyInputDataChanged()
            }
        }
        binding.textviewTermsAndConditions.setOnClickListener {
            try {
                launchOpenPdf()
            } catch (e: CheckoutException) {
                component.onExceptionHappen(e)
            }
        }
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        // no implementation
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        // no embedded localized strings on this view
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) = Unit

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        Logger.d(TAG, "onItemSelected - " + issuersAdapter.getItem(position).name)
        component.inputData.selectedIssuer = issuersAdapter.getItem(position)
        component.notifyInputDataChanged()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        // nothing changed
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.autoCompleteTextViewIssuers.isEnabled = enabled
        binding.textInputLayoutIssuers.isEnabled = enabled
    }

    private fun launchOpenPdf() {
        val url = component.getTermsAndConditionsUrl()
        if (url.isEmpty()) throw ComponentException("Terms and conditions file URL is empty.")
        val uri = Uri.parse(url)
        if (launchNative(uri)) return
        if (launchWithCustomTabs(uri)) return
        if (launchBrowser(uri)) return
        Logger.e(TAG, "Could not launch url")
        throw ComponentException("failed to open terms and conditions pdf.")
    }

    private fun launchNative(uri: Uri): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) launchNativeApi30(uri)
        // because custom tabs pdf viewer is working before api 30 on chrome
        else launchWithCustomTabs(uri)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun launchNativeApi30(uri: Uri): Boolean {
        val nativeAppIntent = Intent().apply {
            action = ACTION_VIEW
            setDataAndType(uri, "application/pdf")
        }
        return try {
            context.startActivity(nativeAppIntent)
            Logger.d(TAG, "launchNativeApi30 - open terms and conditions pdf successful with native app")
            true
        } catch (ex: ActivityNotFoundException) {
            Logger.d(TAG, "launchNativeApi30 - could not find native app to terms and conditions pdf with", ex)
            false
        }
    }

    private fun launchWithCustomTabs(uri: Uri): Boolean {
        // open in custom tabs if there's no native app for the target uri
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()
        return try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(defaultColors)
                .build()
                .launchUrl(context, uri)
            Logger.d(TAG, "launchWithCustomTabs - open terms and conditions pdf successful with custom tabs")
            true
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "launchWithCustomTabs - device doesn't support custom tabs or chrome is disabled", e)
            false
        }
    }

    /**
     * in case the device doesn't support custom tabs or doesn't support google services (Huawei device).
     */
    private fun launchBrowser(uri: Uri): Boolean {
        return try {
            val browserActivityIntent = Intent()
                .setAction(ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(uri)
            context.startActivity(browserActivityIntent)
            Logger.d(TAG, "launchBrowser - open terms and conditions pdf successful with browser")
            true
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "launchBrowser - could not open pdf on browser or there's no browser!", e)
            false
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
