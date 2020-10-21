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

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public final class Card extends ModelObject {
    @NonNull
    public static final Creator<Card> CREATOR = new Creator<>(Card.class);

    private static final String CVC = "cvc";
    private static final String EXPIRY_MONTH = "expiryMonth";
    private static final String EXPIRY_YEAR = "expiryYear";
    private static final String HOLDER_NAME = "holderName";
    private static final String ISSUE_NUMBER = "issueNumber";
    private static final String NUMBER = "number";
    private static final String START_MONTH = "startMonth";
    private static final String START_YEAR = "startYear";

    @NonNull
    public static final Serializer<Card> SERIALIZER = new Serializer<Card>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull Card modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(CVC, modelObject.getCvc());
                jsonObject.putOpt(EXPIRY_MONTH, modelObject.getExpiryMonth());
                jsonObject.putOpt(EXPIRY_YEAR, modelObject.getExpiryYear());
                jsonObject.putOpt(HOLDER_NAME, modelObject.getHolderName());
                jsonObject.putOpt(ISSUE_NUMBER, modelObject.getIssueNumber());
                jsonObject.putOpt(NUMBER, modelObject.getNumber());
                jsonObject.putOpt(START_MONTH, modelObject.getStartMonth());
                jsonObject.putOpt(START_YEAR, modelObject.getStartYear());
            } catch (JSONException e) {
                throw new ModelSerializationException(Card.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public Card deserialize(@NonNull JSONObject jsonObject) {
            final Card card = new Card();
            card.setCvc(jsonObject.optString(CVC));
            card.setExpiryMonth(jsonObject.optString(EXPIRY_MONTH, null));
            card.setExpiryYear(jsonObject.optString(EXPIRY_YEAR, null));
            card.setHolderName(jsonObject.optString(HOLDER_NAME, null));
            card.setIssueNumber(jsonObject.optString(ISSUE_NUMBER, null));
            card.setNumber(jsonObject.optString(NUMBER, null));
            card.setStartMonth(jsonObject.optString(START_MONTH, null));
            card.setStartYear(jsonObject.optString(START_YEAR, null));
            return card;
        }
    };

    private String cvc;
    private String expiryMonth;
    private String expiryYear;
    private String holderName;
    private String issueNumber;
    private String number;
    private String startMonth;
    private String startYear;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getCvc() {
        return cvc;
    }

    @Nullable
    public String getExpiryMonth() {
        return expiryMonth;
    }

    @Nullable
    public String getExpiryYear() {
        return expiryYear;
    }

    @Nullable
    public String getHolderName() {
        return holderName;
    }

    @Nullable
    public String getIssueNumber() {
        return issueNumber;
    }

    @Nullable
    public String getNumber() {
        return number;
    }

    @Nullable
    public String getStartMonth() {
        return startMonth;
    }

    @Nullable
    public String getStartYear() {
        return startYear;
    }

    public void setCvc(@Nullable String cvc) {
        this.cvc = cvc;
    }

    public void setExpiryMonth(@Nullable String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public void setExpiryYear(@Nullable String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public void setHolderName(@Nullable String holderName) {
        this.holderName = holderName;
    }

    public void setIssueNumber(@Nullable String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public void setNumber(@Nullable String number) {
        this.number = number;
    }

    public void setStartMonth(@Nullable String startMonth) {
        this.startMonth = startMonth;
    }

    public void setStartYear(@Nullable String startYear) {
        this.startYear = startYear;
    }
}
