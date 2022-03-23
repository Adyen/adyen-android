/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/4/2019.
 */
package com.adyen.checkout.components.ui.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.adyen.checkout.components.ui.R

object ThemeUtil {

    @ColorInt
    fun getPrimaryThemeColor(context: Context): Int {
        return getAttributeColor(context, R.attr.colorPrimary)
    }

    @ColorInt
    private fun getAttributeColor(context: Context, @AttrRes attributeColor: Int): Int {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(attributeColor))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
}
