/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/9/2022.
 */

package com.adyen.checkout.mbway.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.util.CountryInfo
import com.adyen.checkout.components.core.internal.util.CountryUtils
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.mbway.R
import com.adyen.checkout.mbway.databinding.MbwayViewBinding
import com.adyen.checkout.mbway.internal.ui.MBWayDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import kotlinx.coroutines.CoroutineScope

internal class MbWayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentView {

    private val binding = MbwayViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: MBWayDelegate

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is MBWayDelegate) throw IllegalArgumentException("Unsupported delegate type")
        this.delegate = delegate
        this.localizedContext = localizedContext

        binding.phoneNumberInput.initialize(delegate, localizedContext)
        val countries = delegate.getSupportedCountries().mapToCountryModel()
        binding.phoneNumberInput.setCountries(countries)
    }

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        binding.phoneNumberInput.highlightValidationErrors(delegate.phoneNumberOutputData)
    }

    override fun getView(): View = this

    private fun List<CountryInfo>.mapToCountryModel() = map {
        CountryModel(
            isoCode = it.isoCode,
            countryName = CountryUtils.getCountryName(it.isoCode, delegate.componentParams.shopperLocale),
            callingCode = it.callingCode,
            emoji = it.emoji
        )
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
