/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/9/2022.
 */

package com.adyen.checkout.qrcode

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType

internal class QrCodeViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentViewNew {
        return if (viewType == QrCodeComponentViewType) QrCodeViewNew(context, attrs, defStyleAttr)
        else throw IllegalArgumentException("Unsupported view type")
    }
}

internal object QrCodeComponentViewType : ComponentViewType
