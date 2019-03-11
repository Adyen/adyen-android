/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 15/11/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.model.Authentication;

import org.json.JSONException;
import org.json.JSONObject;

@ProvidedBy(FingerprintAuthentication.class)
public final class FingerprintAuthentication extends JsonObject implements Authentication {
    public static final Parcelable.Creator<FingerprintAuthentication> CREATOR = new DefaultCreator<>(FingerprintAuthentication.class);

    private static final String KEY_THREE_DS_FINGERPRINT_TOKEN = "threeds2.fingerprintToken";

    private final String mFingerprintToken;

    protected FingerprintAuthentication(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mFingerprintToken = jsonObject.getString(KEY_THREE_DS_FINGERPRINT_TOKEN);
    }

    @NonNull
    public String getFingerprintToken() {
        return mFingerprintToken;
    }
}
