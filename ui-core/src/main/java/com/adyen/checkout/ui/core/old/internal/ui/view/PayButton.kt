/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class PayButton(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
) : FrameLayout(context, attrs, defStyleAttr) {

    abstract fun initialize(delegate: ButtonDelegate, coroutineScope: CoroutineScope)

    abstract override fun setEnabled(enabled: Boolean)

    abstract override fun setOnClickListener(listener: OnClickListener?)

    abstract fun setText(text: String?)
}
