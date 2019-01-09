/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 25/01/2018.
 */

package com.adyen.checkout.core.card.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.base.internal.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public final class PublicKeyResponse extends JsonObject {
    @NonNull
    public static final Creator<PublicKeyResponse> CREATOR = new DefaultCreator<>(PublicKeyResponse.class);

    private final String mStatus;

    private final String mId;

    private final String mPublicKey;

    private PublicKeyResponse(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mStatus = jsonObject.getString("status");
        mId = jsonObject.optString("id");
        mPublicKey = jsonObject.optString("publicKey");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PublicKeyResponse that = (PublicKeyResponse) o;

        if (mStatus != null ? !mStatus.equals(that.mStatus) : that.mStatus != null) {
            return false;
        }
        if (mId != null ? !mId.equals(that.mId) : that.mId != null) {
            return false;
        }
        return mPublicKey != null ? mPublicKey.equals(that.mPublicKey) : that.mPublicKey == null;
    }

    @Override
    public int hashCode() {
        int result = mStatus != null ? mStatus.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mId != null ? mId.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mPublicKey != null ? mPublicKey.hashCode() : 0);
        return result;
    }

    @NonNull
    public String getStatus() {
        return mStatus;
    }

    @Nullable
    public String getId() {
        return mId;
    }

    @Nullable
    public String getPublicKey() {
        return mPublicKey;
    }
}
