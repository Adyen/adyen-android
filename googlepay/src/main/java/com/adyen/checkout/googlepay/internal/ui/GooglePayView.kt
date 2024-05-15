/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/5/2024.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.googlepay.databinding.ViewGooglePayBinding
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import kotlinx.coroutines.CoroutineScope

internal class GooglePayView internal constructor(
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
    }

    private fun initializeFragment(delegate: GooglePayDelegate) {
        binding.fragmentContainer.getFragment<GooglePayFragment?>()?.initialize(delegate)
    }

    override fun highlightValidationErrors() = Unit

    override fun getView(): View = this
}
