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
import androidx.annotation.Nullable;

import com.adyen.checkout.base.ActionComponentProvider;
import com.adyen.checkout.base.component.ActionComponentProviderImpl;
import com.adyen.checkout.base.component.BaseActionComponent;
import com.adyen.checkout.base.model.payments.response.Action;
import com.adyen.checkout.base.model.payments.response.SdkAction;
import com.adyen.checkout.base.model.payments.response.WeChatPaySdkAction;
import com.adyen.checkout.base.model.payments.response.WeChatPaySdkData;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeChatPayActionComponent extends BaseActionComponent<WeChatPayActionConfiguration> {
    private static final String TAG = LogUtil.getTag();

    public static final ActionComponentProvider<WeChatPayActionComponent> PROVIDER =
            new ActionComponentProviderImpl<>(WeChatPayActionComponent.class, WeChatPayActionConfiguration.class);

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

    public WeChatPayActionComponent(@NonNull Application application, @Nullable WeChatPayActionConfiguration configuration) {
        super(application, configuration);
        mApi = WXAPIFactory.createWXAPI(application, null, true);
    }

    /**
     * Pass the result Intent from the WeChatPay SDK response on {@link Activity#onNewIntent(Intent)}.
     * You can check if the Intent is correct by calling {@link WeChatPayUtils#isResultIntent(Intent)}
     *
     * @param intent The intent result from WeChatPay SDK.
     */
    public void handleResultIntent(@Nullable Intent intent) {
        // TODO check intent identifiers
        if (intent != null) {
            mApi.handleIntent(intent, mEventHandler);
        } else {
            throw new ComponentException("Intent result is null.");
        }
    }

    @NonNull
    @Override
    protected List<String> getSupportedActionTypes() {
        final String[] supportedCodes = {SdkAction.ACTION_TYPE, WeChatPaySdkAction.ACTION_TYPE};
        return Collections.unmodifiableList(Arrays.asList(supportedCodes));
    }

    @Override
    protected void handleActionInternal(@NonNull Activity activity, @NonNull Action action) throws ComponentException {
        Logger.d(TAG, "handleActionInternal: activity - " + activity.getLocalClassName());
        //noinspection unchecked
        final SdkAction<WeChatPaySdkData> weChatAction = (SdkAction<WeChatPaySdkData>) action;
        if (weChatAction.getSdkData() != null) {
            final boolean weChatInitiated = initiateWeChatPayRedirect(weChatAction.getSdkData(), activity.getLocalClassName());
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
