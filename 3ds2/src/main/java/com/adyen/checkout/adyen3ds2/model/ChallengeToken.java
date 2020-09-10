/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */

package com.adyen.checkout.adyen3ds2.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass", "AbbreviationAsWordInName"})
public class ChallengeToken extends ModelObject {
    @NonNull
    public static final Creator<ChallengeToken> CREATOR = new Creator<>(ChallengeToken.class);

    private static final String ACS_REFERENCE_NUMBER = "acsReferenceNumber";
    private static final String ACS_SIGNED_CONTENT = "acsSignedContent";
    private static final String ACS_TRANS_ID = "acsTransID";
    private static final String ACS_URL = "acsURL";
    private static final String MESSAGE_VERSION = "messageVersion";
    private static final String THREEDS_SERVER_TRANS_ID = "threeDSServerTransID";

    @NonNull
    public static final Serializer<ChallengeToken> SERIALIZER = new Serializer<ChallengeToken>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull ChallengeToken modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(ACS_REFERENCE_NUMBER, modelObject.getAcsReferenceNumber());
                jsonObject.putOpt(ACS_SIGNED_CONTENT, modelObject.getAcsSignedContent());
                jsonObject.putOpt(ACS_TRANS_ID, modelObject.getAcsTransID());
                jsonObject.putOpt(ACS_URL, modelObject.getAcsURL());
                jsonObject.putOpt(MESSAGE_VERSION, modelObject.getMessageVersion());
                jsonObject.putOpt(THREEDS_SERVER_TRANS_ID, modelObject.getThreeDSServerTransID());

            } catch (JSONException e) {
                throw new ModelSerializationException(ChallengeToken.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public ChallengeToken deserialize(@NonNull JSONObject jsonObject) {
            final ChallengeToken challengeToken = new ChallengeToken();
            challengeToken.setAcsReferenceNumber(jsonObject.optString(ACS_REFERENCE_NUMBER, null));
            challengeToken.setAcsSignedContent(jsonObject.optString(ACS_SIGNED_CONTENT, null));
            challengeToken.setAcsTransID(jsonObject.optString(ACS_TRANS_ID, null));
            challengeToken.setAcsURL(jsonObject.optString(ACS_URL, null));
            challengeToken.setMessageVersion(jsonObject.optString(MESSAGE_VERSION, null));
            challengeToken.setThreeDSServerTransID(jsonObject.optString(THREEDS_SERVER_TRANS_ID, null));
            return challengeToken;
        }
    };

    private String acsReferenceNumber;
    private String acsSignedContent;
    private String acsTransID;
    private String acsURL;
    private String messageVersion;
    private String threeDSServerTransID;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getAcsReferenceNumber() {
        return acsReferenceNumber;
    }

    public void setAcsReferenceNumber(@Nullable String acsReferenceNumber) {
        this.acsReferenceNumber = acsReferenceNumber;
    }

    @Nullable
    public String getAcsSignedContent() {
        return acsSignedContent;
    }

    public void setAcsSignedContent(@Nullable String acsSignedContent) {
        this.acsSignedContent = acsSignedContent;
    }

    @Nullable
    public String getAcsTransID() {
        return acsTransID;
    }

    public void setAcsTransID(@Nullable String acsTransID) {
        this.acsTransID = acsTransID;
    }

    @Nullable
    public String getAcsURL() {
        return acsURL;
    }

    public void setAcsURL(@Nullable String acsURL) {
        this.acsURL = acsURL;
    }

    @Nullable
    public String getMessageVersion() {
        return messageVersion;
    }

    public void setMessageVersion(@Nullable String messageVersion) {
        this.messageVersion = messageVersion;
    }

    @Nullable
    public String getThreeDSServerTransID() {
        return threeDSServerTransID;
    }

    public void setThreeDSServerTransID(@Nullable String threeDSServerTransID) {
        this.threeDSServerTransID = threeDSServerTransID;
    }
}
