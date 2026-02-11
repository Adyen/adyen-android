/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/4/2024.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.adyen.checkout.dropin.databinding.BottomSheetToolbarBinding
import com.adyen.checkout.dropin.internal.ui.DropInBottomSheetToolbarMode.BACK_BUTTON
import com.adyen.checkout.dropin.internal.ui.DropInBottomSheetToolbarMode.CLOSE_BUTTON
import com.adyen.checkout.dropin.internal.ui.DropInBottomSheetToolbarMode.NO_BUTTON

internal class DropInBottomSheetToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = BottomSheetToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setMode(NO_BUTTON)
        ViewCompat.setAccessibilityHeading(binding.textViewTitle, true)
    }

    fun setTitle(title: String?) {
        binding.textViewTitle.text = title
    }

    fun setOnButtonClickListener(listener: OnClickListener?) = with(binding) {
        imageViewBack.setOnClickListener(listener)
        imageViewClose.setOnClickListener(listener)
    }

    fun setMode(toolbarMode: DropInBottomSheetToolbarMode) = when (toolbarMode) {
        BACK_BUTTON -> {
            setBackButtonVisibility(true)
            setCloseButtonVisibility(false)
        }

        CLOSE_BUTTON -> {
            setBackButtonVisibility(false)
            setCloseButtonVisibility(true)
        }

        NO_BUTTON -> {
            setBackButtonVisibility(false)
            setCloseButtonVisibility(false)
        }
    }

    private fun setBackButtonVisibility(isVisible: Boolean) {
        binding.imageViewBack.isVisible = isVisible
    }

    private fun setCloseButtonVisibility(isVisible: Boolean) {
        binding.imageViewClose.isVisible = isVisible
    }
}

internal enum class DropInBottomSheetToolbarMode {
    BACK_BUTTON,
    CLOSE_BUTTON,
    NO_BUTTON,
}
