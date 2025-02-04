/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/2/2025.
 */

package com.adyen.checkout.example.extensions

import android.app.Activity
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding

internal fun Activity.applyInsetsToRootLayout(binding: ViewBinding) {
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        view.updateLayoutParams<MarginLayoutParams> {
            leftMargin = insets.left
            bottomMargin = insets.bottom
            rightMargin = insets.right
            topMargin = insets.top
        }
        WindowInsetsCompat.CONSUMED
    }
}
