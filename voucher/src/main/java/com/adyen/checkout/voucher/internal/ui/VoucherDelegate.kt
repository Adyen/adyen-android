/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher.internal.ui

import android.content.Context
import android.view.View
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.PermissionRequestingDelegate
import com.adyen.checkout.components.core.internal.ui.ViewableDelegate
import com.adyen.checkout.core.old.internal.ui.PermissionHandler
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import com.adyen.checkout.voucher.internal.ui.model.VoucherOutputData
import com.adyen.checkout.voucher.internal.ui.model.VoucherUIEvent
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface VoucherDelegate :
    ActionDelegate,
    ViewableDelegate<VoucherOutputData>,
    ViewProvidingDelegate,
    PermissionRequestingDelegate,
    PermissionHandler {

    val eventFlow: Flow<VoucherUIEvent>
    fun downloadVoucher(context: Context)
    fun saveVoucherAsImage(context: Context, view: View)
}
