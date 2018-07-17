package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.internal.model.WeChatPaySdkRedirectDataImpl;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by ran on 26/04/2018.
 */
@ProvidedBy(WeChatPaySdkRedirectDataImpl.class)
public interface WeChatPaySdkRedirectData extends RedirectData {
    /**
     * @return The app ID for the redirect to the WeChatPay app.
     */
    @NonNull
    String getAppId();

    /**
     * @return The partner ID for the redirect to the WeChatPay app.
     */
    @NonNull
    String getPartnerId();

    /**
     * @return The prepay ID for the redirect to the WeChatPay app.
     */
    @NonNull
    String getPrepayId();

    /**
     * @return The timestamp for the redirect to the WeChatPay app.
     */
    @NonNull
    String getTimestamp();

    /**
     * @return The package value for the redirect to the WeChatPay app.
     */
    @NonNull
    String getPackageValue();

    /**
     * @return The nonce for the redirect to the WeChatPay app.
     */
    @NonNull
    String getNonceStr();

    /**
     * @return The signature for the redirect to the WeChatPay app.
     */
    @NonNull
    String getSignature();
}
