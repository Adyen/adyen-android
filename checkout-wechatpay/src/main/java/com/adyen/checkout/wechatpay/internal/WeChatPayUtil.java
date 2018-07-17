package com.adyen.checkout.wechatpay.internal;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.WeChatPayDetails;
import com.adyen.checkout.core.model.WeChatPaySdkRedirectData;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.lang.ref.WeakReference;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by ran on 26/04/2018.
 */
public final class WeChatPayUtil implements IWXAPIEventHandler {
    private final WeChatPayProvider mProvider;

    private final WeakReference<WeChatPayListener> mListenerWeakReference;

    private final IWXAPI mApi;

    public static boolean isAvailable(@NonNull Context context) {
        IWXAPI api = WXAPIFactory.createWXAPI(context.getApplicationContext(), null, true);
        boolean isAvailable = api.isWXAppInstalled() && api.isWXAppSupportAPI();
        api.detach();
        return isAvailable;
    }

    @NonNull
    public static WeChatPayUtil get(@NonNull Application application, @NonNull WeChatPayProvider provider, @NonNull WeChatPayListener listener) {
        return new WeChatPayUtil(application, provider, listener);
    }

    private WeChatPayUtil(@NonNull Application application, @NonNull WeChatPayProvider provider, @NonNull WeChatPayListener listener) {
        mProvider = provider;
        mListenerWeakReference = new WeakReference<>(listener);
        mApi = WXAPIFactory.createWXAPI(application, null, true);
    }

    @NonNull
    public IWXAPI getApi() {
        return mApi;
    }

    @CheckResult
    public boolean initiateWeChatPayRedirect(@NonNull WeChatPaySdkRedirectData weChatPaySdkRedirectData) {
        mApi.registerApp(weChatPaySdkRedirectData.getAppId());

        return mApi.sendReq(generatePayRequest(weChatPaySdkRedirectData));
    }

    public void handleIntent(@NonNull Intent intent) {
        mApi.handleIntent(intent, this);
    }

    public void detach() {
        mApi.detach();
    }

    @Override
    public void onReq(BaseReq baseReq) {
        // Do nothing.
    }

    @Override
    public void onResp(BaseResp baseResp) {
        WeChatPayListener weChatPayListener = mListenerWeakReference.get();
        if (weChatPayListener == null) {
            throw new RuntimeException("The weak reference to " + WeChatPayListener.class.getName() + " is null");
        }

        WeChatPayDetails weChatPayDetails = new WeChatPayDetails.Builder(String.valueOf(baseResp.errCode)).build();
        weChatPayListener.onPaymentDetails(baseResp, weChatPayDetails);
    }

    @NonNull
    private PayReq generatePayRequest(@NonNull WeChatPaySdkRedirectData weChatPaySdkRedirectData) {
        PayReq request = new PayReq();

        request.appId = weChatPaySdkRedirectData.getAppId();
        request.partnerId = weChatPaySdkRedirectData.getPartnerId();
        request.prepayId = weChatPaySdkRedirectData.getPrepayId();
        request.packageValue = weChatPaySdkRedirectData.getPackageValue();
        request.nonceStr = weChatPaySdkRedirectData.getNonceStr();
        request.timeStamp = weChatPaySdkRedirectData.getTimestamp();
        request.sign = weChatPaySdkRedirectData.getSignature();

        request.options = new PayReq.Options();
        request.options.callbackClassName = mProvider.getCallbackActivityName();

        return request;
    }
}
