/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */

package com.adyen.checkout.base.model.paymentmethods;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public final class StoredDetails extends ModelObject {
    @NonNull
    public static final Creator<StoredDetails> CREATOR = new Creator<>(StoredDetails.class);

    private static final String BANK = "bank";
    private static final String CARD = "card";
    private static final String EMAIL_ADDRESS = "emailAddress";

    @NonNull
    public static final Serializer<StoredDetails> SERIALIZER = new Serializer<StoredDetails>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull StoredDetails modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(BANK, modelObject.getBank());
                jsonObject.putOpt(CARD, modelObject.getCard());
                jsonObject.putOpt(EMAIL_ADDRESS, modelObject.getEmailAddress());
            } catch (JSONException e) {
                throw new ModelSerializationException(StoredDetails.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public StoredDetails deserialize(@NonNull JSONObject jsonObject) {
            final StoredDetails storedDetails = new StoredDetails();
            storedDetails.setBank(ModelUtils.deserializeOpt(jsonObject.optJSONObject(BANK), Bank.SERIALIZER));
            storedDetails.setCard(ModelUtils.deserializeOpt(jsonObject.optJSONObject(CARD), Card.SERIALIZER));
            storedDetails.setEmailAddress(jsonObject.optString(EMAIL_ADDRESS));
            return storedDetails;
        }
    };

    private Bank bank;
    private Card card;
    private String emailAddress;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public Bank getBank() {
        return bank;
    }

    @Nullable
    public Card getCard() {
        return card;
    }

    @Nullable
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setBank(@Nullable Bank bank) {
        this.bank = bank;
    }

    public void setCard(@Nullable Card card) {
        this.card = card;
    }

    public void setEmailAddress(@Nullable String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
