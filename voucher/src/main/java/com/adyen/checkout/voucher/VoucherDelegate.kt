/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher

import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.ViewableDelegate
import com.adyen.checkout.components.model.payments.response.VoucherAction

interface VoucherDelegate : ActionDelegate<VoucherAction>, ViewableDelegate<VoucherOutputData>
