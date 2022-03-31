/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/10/2019.
 */

package com.adyen.checkout.wechatpay;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.ActionComponentProvider;
import com.adyen.checkout.components.base.BaseActionComponent;
import com.adyen.checkout.components.base.IntentHandlingComponent;
import com.adyen.checkout.components.model.payments.response.Action;
import com.adyen.checkout.components.model.payments.response.SdkAction;
import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.jetbrains.annotations.NotNull;

public class WeChatPayActionComponent extends BaseActionComponent<WeChatPayActionConfiguration>
        implements IntentHandlingComponent {
    private static final String TAG = LogUtil.getTag();

    public static final ActionComponentProvider<WeChatPayActionComponent, WeChatPayActionConfiguration> PROVIDER =
            new WeChatPayActionComponentProvider();

    private final IWXAPI mApi;

    private final IWXAPIEventHandler mEventHandler = new IWXAPIEventHandler() {
        @Override
        public void onReq(BaseReq baseReq) {
            // Do nothing.
        }

        @Override
        public void onResp(BaseResp baseResp) {
            if (baseResp != null) {
                notifyDetails(WeChatPayUtils.parseResult(baseResp));
            } else {
                notifyException(new ComponentException("WeChatPay SDK baseResp is null."));
            }
        }
    };

    public WeChatPayActionComponent(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull Application application,
            @NonNull WeChatPayActionConfiguration configuration
    ) {
        super(savedStateHandle, application, configuration);
        mApi = WXAPIFactory.createWXAPI(application, null, true);
    }

    /**
     * Pass the result Intent from the WeChatPay SDK response on Activity#onNewIntent(Intent).
     * You can check if the Intent is correct by calling {@link WeChatPayUtils#isResultIntent(Intent)}
     *
     * @param intent The intent result from WeChatPay SDK.
     */
    @Override
    public void handleIntent(@NotNull Intent intent) {
        // TODO check intent identifiers
        mApi.handleIntent(intent, mEventHandler);
    }

    @Override
    public boolean canHandleAction(@NonNull Action action) {
        return PROVIDER.canHandleAction(action);
    }

    @Override
    protected void handleActionInternal(@NonNull Activity activity, @NonNull Action action) throws ComponentException {
        Logger.d(TAG, "handleActionInternal: activity - " + activity.getLocalClassName());
        //noinspection unchecked
        final SdkAction<WeChatPaySdkData> weChatAction = (SdkAction<WeChatPaySdkData>) action;
        if (weChatAction.getSdkData() != null) {
            final boolean weChatInitiated = initiateWeChatPayRedirect(weChatAction.getSdkData(), activity.getClass().getName());
            if (!weChatInitiated) {
                throw new ComponentException("Failed to initialize WeChat app.");
            }
        } else {
            throw new ComponentException("WeChatPay Data not found.");
        }
    }

    private boolean initiateWeChatPayRedirect(@NonNull WeChatPaySdkData weChatPaySdkData, @NonNull String callbackActivityName) {
        Logger.d(TAG, "initiateWeChatPayRedirect");
        mApi.registerApp(weChatPaySdkData.getAppid());
        return mApi.sendReq(WeChatPayUtils.generatePayRequest(weChatPaySdkData, callbackActivityName));
    }
}
