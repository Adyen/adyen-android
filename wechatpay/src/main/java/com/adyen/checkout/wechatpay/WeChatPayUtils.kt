/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */
package com.adyen.checkout.wechatpay

import android.content.Intent

object WeChatPayUtils {

    private const val RESULT_EXTRA_KEY = "_wxapi_baseresp_errstr"

    fun isResultIntent(intent: Intent?): Boolean {
        return intent?.extras?.containsKey(RESULT_EXTRA_KEY) == true
    }
}
