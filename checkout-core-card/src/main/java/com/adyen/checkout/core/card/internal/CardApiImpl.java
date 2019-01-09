/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 25/01/2018.
 */

package com.adyen.checkout.core.card.internal;

import android.app.Application;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.adyen.checkout.base.HostProvider;
import com.adyen.checkout.base.internal.Api;
import com.adyen.checkout.base.internal.Json;
import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.card.CardApi;

import org.json.JSONObject;

import java.util.concurrent.Callable;

public final class CardApiImpl extends CardApi {
    // %1$s = publicKeyToken
    private static final String PUBLIC_KEY_PATH = "hpp/cse/%1$s/json.shtml";

    private static CardApiImpl sInstance;

    private final String mPublicKeyUrlFormat;

    @NonNull
    public static synchronized CardApiImpl getInstance(@NonNull Application application, @NonNull HostProvider hostProvider) {
        if (sInstance == null || isDifferentHost(sInstance, hostProvider)) {
            sInstance = new CardApiImpl(hostProvider);
        }

        return sInstance;
    }

    private static boolean isDifferentHost(@NonNull CardApiImpl cardApi, @NonNull HostProvider hostProvider) {
        return !cardApi.mPublicKeyUrlFormat.startsWith(hostProvider.getUrl());
    }

    private CardApiImpl(@NonNull HostProvider hostProvider) {
        mPublicKeyUrlFormat = hostProvider.getUrl() + PUBLIC_KEY_PATH;
    }

    @NonNull
    @Override
    public Callable<String> getPublicKey(@NonNull final String publicKeyToken) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                String url = String.format(mPublicKeyUrlFormat, publicKeyToken);
                byte[] responseBytes = get(url, Json.getDefaultHeaders());
                String jsonString = new String(responseBytes, Api.CHARSET);
                JSONObject jsonObject = new JSONObject(jsonString);
                PublicKeyResponse publicKeyResponse = JsonObject.parseFrom(jsonObject, PublicKeyResponse.class);
                String publicKey = publicKeyResponse.getPublicKey();

                if (TextUtils.isEmpty(publicKey)) {
                    throw new Exception("Public key is empty.");
                } else {
                    return publicKey;
                }
            }
        };
    }
}
