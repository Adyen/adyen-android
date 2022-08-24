/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2022.
 */

package com.adyen.checkout.wechatpay

import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.IntentHandlingDelegate
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData

interface WeChatDelegate : ActionDelegate<SdkAction<WeChatPaySdkData>>, DetailsEmittingDelegate, IntentHandlingDelegate
