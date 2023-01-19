/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/1/2023.
 */

package com.adyen.checkout.ach

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.ui.ComponentView
import kotlinx.coroutines.CoroutineScope

internal class AchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context,
    attrs,
    defStyleAttr
),
    ComponentView {

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is AchDelegate) throw IllegalArgumentException("Unsupported delegate type")
    }

    override fun highlightValidationErrors() {
        TODO("Not yet implemented")
    }

    override fun getView(): View {
        TODO("Not yet implemented")
    }
}
