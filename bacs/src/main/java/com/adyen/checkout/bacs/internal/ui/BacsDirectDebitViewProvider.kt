/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/9/2022.
 */

package com.adyen.checkout.bacs.internal.ui

import android.content.Context
import com.adyen.checkout.bacs.R
import com.adyen.checkout.bacs.internal.ui.view.BacsDirectDebitConfirmationView
import com.adyen.checkout.bacs.internal.ui.view.BacsDirectDebitInputView
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object BacsDirectDebitViewProvider : ViewProvider {
    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            BacsComponentViewType.INPUT -> BacsDirectDebitInputView(context)
            BacsComponentViewType.CONFIRMATION -> BacsDirectDebitConfirmationView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal enum class BacsComponentViewType(override val buttonTextResId: Int) :
    ButtonComponentViewType {
    INPUT(R.string.bacs_continue),
    CONFIRMATION(R.string.bacs_confirm_and_pay);

    override val viewProvider: ViewProvider = BacsDirectDebitViewProvider
}
