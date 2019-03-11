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

@ProvidedBy(ChallengeAuthentication.class)
public final class ChallengeAuthentication extends JsonObject implements Authentication {
    public static final Parcelable.Creator<ChallengeAuthentication> CREATOR = new DefaultCreator<>(ChallengeAuthentication.class);

    private static final String KEY_THREE_DS_CHALLENGE_TOKEN = "threeds2.challengeToken";

    private final String mChallengeToken;

    protected ChallengeAuthentication(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mChallengeToken = jsonObject.getString(KEY_THREE_DS_CHALLENGE_TOKEN);
    }

    @NonNull
    public String getChallengeToken() {
        return mChallengeToken;
    }
}
