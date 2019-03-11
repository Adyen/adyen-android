/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 19/11/2018.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public final class ChallengeDetails extends PaymentMethodDetails {
    public static final Creator<ChallengeDetails> CREATOR = new Creator<ChallengeDetails>() {
        @Override
        public ChallengeDetails createFromParcel(Parcel source) {
            return new ChallengeDetails(source);
        }

        @Override
        public ChallengeDetails[] newArray(int size) {
            return new ChallengeDetails[size];
        }
    };

    public static final String KEY_THREE_DS_CHALLENGE_RESULT = "threeds2.challengeResult";

    private final String mChallengeResult;

    public ChallengeDetails(@NonNull String challengeResult) {
        mChallengeResult = challengeResult;
    }

    protected ChallengeDetails(@NonNull Parcel in) {
        super(in);

        this.mChallengeResult = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.mChallengeResult);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_THREE_DS_CHALLENGE_RESULT, mChallengeResult);

        return jsonObject;
    }
}
