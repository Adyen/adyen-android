/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 19/11/2018.
 */

package com.adyen.checkout.threeds.internal.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonEncodable;
import com.adyen.threeds2.AuthenticationRequestParameters;

import org.json.JSONException;
import org.json.JSONObject;

public final class Fingerprint extends JsonEncodable {

    private final AuthenticationRequestParameters mAuthenticationRequestParameters;

    public Fingerprint(@NonNull AuthenticationRequestParameters authenticationRequestParameters) {
        mAuthenticationRequestParameters = authenticationRequestParameters;
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sdkAppID", mAuthenticationRequestParameters.getSDKAppID());
        jsonObject.put("sdkEncData", mAuthenticationRequestParameters.getDeviceData());
        jsonObject.put("sdkEphemPubKey", new JSONObject(mAuthenticationRequestParameters.getSDKEphemeralPublicKey()));
        jsonObject.put("sdkReferenceNumber", mAuthenticationRequestParameters.getSDKReferenceNumber());
        jsonObject.put("sdkTransID", mAuthenticationRequestParameters.getSDKTransactionID());

        return jsonObject;
    }

    @NonNull
    public AuthenticationRequestParameters getAuthenticationRequestParameters() {
        return mAuthenticationRequestParameters;
    }
}
