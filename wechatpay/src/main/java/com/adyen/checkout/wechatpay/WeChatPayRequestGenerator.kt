/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2022.
 */

package com.adyen.checkout.wechatpay

import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelpay.PayReq

internal interface WeChatRequestGenerator<T : BaseReq> {
    fun generate(weChatPaySdkData: WeChatPaySdkData, callbackActivityName: String): T
}

internal class WeChatPayRequestGenerator : WeChatRequestGenerator<PayReq> {

    override fun generate(weChatPaySdkData: WeChatPaySdkData, callbackActivityName: String): PayReq {
        return PayReq().apply {
            appId = weChatPaySdkData.appid
            partnerId = weChatPaySdkData.partnerid
            prepayId = weChatPaySdkData.prepayid
            packageValue = weChatPaySdkData.packageValue
            nonceStr = weChatPaySdkData.noncestr
            timeStamp = weChatPaySdkData.timestamp
            sign = weChatPaySdkData.sign
            options = PayReq.Options()
            options.callbackClassName = callbackActivityName
        }
    }
}
