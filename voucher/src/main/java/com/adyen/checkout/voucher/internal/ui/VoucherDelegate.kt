/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher.internal.ui

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.ViewableDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface VoucherDelegate :
    ActionDelegate,
    ViewableDelegate<VoucherOutputData>,
    ViewProvidingDelegate {
    fun downloadVoucher(context: Context)
}
