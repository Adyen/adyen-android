/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/11/2023.
 */

package com.adyen.checkout.card.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.card.databinding.AddressLookupViewBinding
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import kotlinx.coroutines.CoroutineScope

internal class AddressLookupView @JvmOverloads constructor(
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

    // TODO address lookup
    @Suppress("UnusedPrivateProperty")
    private val binding: AddressLookupViewBinding = AddressLookupViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var cardDelegate: CardDelegate

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is CardDelegate) { "Unsupported delegate type" }
        cardDelegate = delegate

        // TODO address lookup init views
    }

    // TODO address lookup
    @Suppress("UnusedPrivateProperty")
    override fun highlightValidationErrors() {
        cardDelegate.outputData.let {
            var isErrorFocused = false
            // TODO address lookup validation
        }
    }

    override fun getView(): View = this
}
