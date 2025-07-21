/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/9/2022.
 */

package com.adyen.checkout.qrcode.internal.ui

import android.content.Context
import com.adyen.checkout.qrcode.internal.ui.view.FullQRCodeView
import com.adyen.checkout.qrcode.internal.ui.view.SimpleQRCodeView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.old.internal.ui.view.PaymentInProgressView

internal object QrCodeViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView = when (viewType) {
        QrCodeComponentViewType.SIMPLE_QR_CODE -> SimpleQRCodeView(context)
        QrCodeComponentViewType.FULL_QR_CODE -> FullQRCodeView(context)
        QrCodeComponentViewType.REDIRECT -> PaymentInProgressView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal enum class QrCodeComponentViewType : ComponentViewType {
    SIMPLE_QR_CODE,
    FULL_QR_CODE,
    REDIRECT;

    override val viewProvider: ViewProvider = QrCodeViewProvider
}
