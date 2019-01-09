/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/07/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.model.StoredDetails;

import org.json.JSONException;
import org.json.JSONObject;

public final class StoredDetailsImpl extends JsonObject implements StoredDetails {
    @NonNull
    public static final Parcelable.Creator<StoredDetailsImpl> CREATOR = new DefaultCreator<>(StoredDetailsImpl.class);

    private CardImpl mCard;

    private String mEmailAddress;

    public StoredDetailsImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mCard = parseOptional("card", CardImpl.class);
        mEmailAddress = jsonObject.optString("emailAddress", null);
    }

    @Nullable
    @Override
    public CardImpl getCard() {
        return mCard;
    }

    @Nullable
    @Override
    public String getEmailAddress() {
        return mEmailAddress;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StoredDetailsImpl that = (StoredDetailsImpl) o;

        if (mCard != null ? !mCard.equals(that.mCard) : that.mCard != null) {
            return false;
        }
        return mEmailAddress != null ? mEmailAddress.equals(that.mEmailAddress) : that.mEmailAddress == null;
    }

    @Override
    public int hashCode() {
        int result = mCard != null ? mCard.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mEmailAddress != null ? mEmailAddress.hashCode() : 0);
        return result;
    }
}
