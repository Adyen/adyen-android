/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */

package com.adyen.checkout.googlepay.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class CardParameters extends ModelObject {

    @NonNull
    public static final Creator<CardParameters> CREATOR = new Creator<>(CardParameters.class);

    private static final String ALLOWED_AUTH_METHODS = "allowedAuthMethods";
    private static final String ALLOWED_CARD_NETWORKS = "allowedCardNetworks";
    private static final String ALLOW_PREPAID_CARDS = "allowPrepaidCards";
    private static final String BILLING_ADDRESS_REQUIRED = "billingAddressRequired";
    private static final String BILLING_ADDRESS_PARAMETERS = "billingAddressParameters";

    @NonNull
    public static final Serializer<CardParameters> SERIALIZER = new Serializer<CardParameters>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull CardParameters modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(ALLOWED_AUTH_METHODS, JsonUtils.serializeOptStringList(modelObject.getAllowedAuthMethods()));
                jsonObject.putOpt(ALLOWED_CARD_NETWORKS, JsonUtils.serializeOptStringList(modelObject.getAllowedCardNetworks()));
                jsonObject.putOpt(ALLOW_PREPAID_CARDS, modelObject.isAllowPrepaidCards());
                jsonObject.putOpt(BILLING_ADDRESS_REQUIRED, modelObject.isBillingAddressRequired());
                jsonObject.putOpt(BILLING_ADDRESS_PARAMETERS,
                        ModelUtils.serializeOpt(modelObject.getBillingAddressParameters(), BillingAddressParameters.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(CardParameters.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public CardParameters deserialize(@NonNull JSONObject jsonObject) {
            final CardParameters cardParameters = new CardParameters();
            cardParameters.setAllowedAuthMethods(JsonUtils.parseOptStringList(jsonObject.optJSONArray(ALLOWED_AUTH_METHODS)));
            cardParameters.setAllowedCardNetworks(JsonUtils.parseOptStringList(jsonObject.optJSONArray(ALLOWED_CARD_NETWORKS)));
            cardParameters.setAllowPrepaidCards(jsonObject.optBoolean(ALLOW_PREPAID_CARDS));
            cardParameters.setBillingAddressRequired(jsonObject.optBoolean(BILLING_ADDRESS_REQUIRED));
            cardParameters.setBillingAddressParameters(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(BILLING_ADDRESS_PARAMETERS), BillingAddressParameters.SERIALIZER));
            return cardParameters;
        }
    };

    private List<String> allowedAuthMethods;
    private List<String> allowedCardNetworks;
    private boolean allowPrepaidCards;
    private boolean billingAddressRequired;
    private BillingAddressParameters billingAddressParameters;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public List<String> getAllowedAuthMethods() {
        return allowedAuthMethods;
    }

    public void setAllowedAuthMethods(@Nullable List<String> allowedAuthMethods) {
        this.allowedAuthMethods = allowedAuthMethods;
    }

    @Nullable
    public List<String> getAllowedCardNetworks() {
        return allowedCardNetworks;
    }

    public void setAllowedCardNetworks(@Nullable List<String> allowedCardNetworks) {
        this.allowedCardNetworks = allowedCardNetworks;
    }

    public boolean isAllowPrepaidCards() {
        return allowPrepaidCards;
    }

    public void setAllowPrepaidCards(boolean allowPrepaidCards) {
        this.allowPrepaidCards = allowPrepaidCards;
    }

    public boolean isBillingAddressRequired() {
        return billingAddressRequired;
    }

    public void setBillingAddressRequired(boolean billingAddressRequired) {
        this.billingAddressRequired = billingAddressRequired;
    }

    @Nullable
    public BillingAddressParameters getBillingAddressParameters() {
        return billingAddressParameters;
    }

    public void setBillingAddressParameters(@Nullable BillingAddressParameters billingAddressParameters) {
        this.billingAddressParameters = billingAddressParameters;
    }
}
