/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.old.internal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.googlepay.databinding.ViewGooglePayBinding
import com.adyen.checkout.googlepay.old.internal.ui.model.GooglePayOutputData
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GooglePayView
@JvmOverloads
internal constructor(
    layoutInflater: LayoutInflater,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(layoutInflater.context, attrs, defStyleAttr), ComponentView {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : this(LayoutInflater.from(context), attrs, defStyleAttr)

    private var binding = ViewGooglePayBinding.inflate(layoutInflater, this)

    private var delegate: GooglePayDelegate? = null

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is GooglePayDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        initializeFragment(delegate)

        observeDelegate(delegate, coroutineScope)
    }

    private fun initializeFragment(delegate: GooglePayDelegate) {
        binding.fragmentContainer.getFragment<GooglePayFragment?>()?.initialize(delegate)
    }

    private fun observeDelegate(delegate: GooglePayDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: GooglePayOutputData) {
        binding.processingPaymentView.isVisible = outputData.isLoading
    }

    override fun highlightValidationErrors() = Unit

    override fun getView(): View = this
}
