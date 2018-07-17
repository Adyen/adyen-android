package com.adyen.checkout.wechatpay.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.WeChatPayDetails;
import com.tencent.mm.opensdk.modelbase.BaseResp;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by ran on 09/05/2018.
 */
public interface WeChatPayListener {
    /**
     * Called when the {@link WeChatPayDetails} have been received.
     *
     * @param baseResp The {@link BaseResp} containing the response data from WeChat.
     * @param weChatPayDetails The populated {@link WeChatPayDetails} that can be used for submitting additional details.
     *
     * @see com.adyen.checkout.core.PaymentHandler#submitAdditionalDetails(com.adyen.checkout.core.model.PaymentMethodDetails)
     */
    void onPaymentDetails(@NonNull BaseResp baseResp, @NonNull WeChatPayDetails weChatPayDetails);
}
