/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */

package com.adyen.checkout.components.status.api;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.status.model.StatusRequest;
import com.adyen.checkout.components.status.model.StatusResponse;
import com.adyen.checkout.core.api.Connection;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

class StatusConnection extends Connection<StatusResponse> {
    private static final String TAG = LogUtil.getTag();

    private final StatusRequest mStatusRequest;

    StatusConnection(@NonNull String url, @NonNull StatusRequest statusRequest) {
        super(url);
        mStatusRequest = statusRequest;
    }

    @Override
    public StatusResponse call() throws IOException, JSONException {
        Logger.v(TAG, "call - " + getUrl());
        final byte[] body = StatusRequest.SERIALIZER.serialize(mStatusRequest).toString().getBytes(Charset.defaultCharset());
        final byte[] bytes = post(CONTENT_TYPE_JSON_HEADER, body);
        final String result = new String(bytes, Charset.defaultCharset());
        final JSONObject jsonObject = new JSONObject(result);
        return StatusResponse.SERIALIZER.deserialize(jsonObject);
    }
}
