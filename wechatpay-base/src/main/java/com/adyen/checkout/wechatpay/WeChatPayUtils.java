/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */

package com.adyen.checkout.wechatpay;

import android.app.Application;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.model.payments.response.WeChatPaySdkData;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.NoConstructorException;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

public final class WeChatPayUtils {

    private static final String RESULT_EXTRA_KEY = "_wxapi_baseresp_errstr";

    private static final String RESULT_CODE = "resultCode";

    public static boolean isResultIntent(@Nullable Intent intent) {
        return intent != null && intent.getExtras() != null && intent.getExtras().containsKey(RESULT_EXTRA_KEY);
    }

    static boolean isAvailable(Application applicationContext) {
        final IWXAPI api = WXAPIFactory.createWXAPI(applicationContext, null, true);
        final boolean isAppInstalled = api.isWXAppInstalled();
        final boolean isSupported = Build.PAY_SUPPORTED_SDK_INT <= api.getWXAppSupportAPI();
        api.detach();
        return isAppInstalled && isSupported;
    }

    @NonNull
    static PayReq generatePayRequest(@NonNull WeChatPaySdkData weChatPaySdkData, @NonNull String callbackActivityName) {
        final PayReq request = new PayReq();

        request.appId = weChatPaySdkData.getAppid();
        request.partnerId = weChatPaySdkData.getPartnerid();
        request.prepayId = weChatPaySdkData.getPrepayid();
        request.packageValue = weChatPaySdkData.getPackageValue();
        request.nonceStr = weChatPaySdkData.getNoncestr();
        request.timeStamp = weChatPaySdkData.getTimestamp();
        request.sign = weChatPaySdkData.getSign();

        request.options = new PayReq.Options();
        request.options.callbackClassName = callbackActivityName;

        return request;
    }

    static JSONObject parseResult(@NonNull BaseResp baseResp) {

        final JSONObject result = new JSONObject();

        try {
            result.put(RESULT_CODE, baseResp.errCode);
        } catch (JSONException e) {
            throw new CheckoutException("Error parsing result.", e);
        }

        return result;
    }

    private WeChatPayUtils() {
        throw new NoConstructorException();
    }
}
