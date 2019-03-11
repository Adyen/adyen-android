/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 20/11/2018.
 */

package com.adyen.checkout.threeds.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonDecodable;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("AbbreviationAsWordInName")
public final class Challenge extends JsonDecodable {
    public static final Parcelable.Creator<Challenge> CREATOR = new DefaultCreator<>(Challenge.class);

    private static final String KEY_MESSAGE_VERSION = "messageVersion";

    private static final String KEY_THREE_DS_SERVER_TRANSACTION_ID = "threeDSServerTransID";

    private static final String KEY_ACS_TRANSACTION_ID = "acsTransID";

    private static final String KEY_ACS_REFERENCE_NUMBER = "acsReferenceNumber";

    private static final String KEY_ACS_SIGNED_CONTENT = "acsSignedContent";

    private static final String KEY_ACS_URL = "acsURL";

    private final String mMessageVersion;

    private final String mThreeDSServerTransID;

    private final String mAcsTransID;

    private final String mAcsReferenceNumber;

    private final String mAcsSignedContent;

    private final String mAcsURL;

    public Challenge(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mMessageVersion = jsonObject.getString(KEY_MESSAGE_VERSION);
        mThreeDSServerTransID = jsonObject.getString(KEY_THREE_DS_SERVER_TRANSACTION_ID);
        mAcsTransID = jsonObject.getString(KEY_ACS_TRANSACTION_ID);
        mAcsReferenceNumber = jsonObject.getString(KEY_ACS_REFERENCE_NUMBER);
        mAcsSignedContent = jsonObject.getString(KEY_ACS_SIGNED_CONTENT);
        mAcsURL = jsonObject.getString(KEY_ACS_URL);
    }

    @NonNull
    public String getMessageVersion() {
        return mMessageVersion;
    }

    @NonNull
    public String getThreeDSServerTransID() {
        return mThreeDSServerTransID;
    }

    @NonNull
    public String getAcsTransID() {
        return mAcsTransID;
    }

    @NonNull
    public String getAcsReferenceNumber() {
        return mAcsReferenceNumber;
    }

    @NonNull
    public String getAcsSignedContent() {
        return mAcsSignedContent;
    }

    @NonNull
    public String getAcsURL() {
        return mAcsURL;
    }
}
