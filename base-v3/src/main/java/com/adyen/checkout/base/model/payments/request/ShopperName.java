/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/12/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass", "DeclarationOrder", "PMD.FieldDeclarationsShouldBeAtStartOfClass"})
public class ShopperName extends ModelObject {
    @NonNull
    public static final Creator<ShopperName> CREATOR = new Creator<>(ShopperName.class);

    private static final String FIRST_NAME = "firstName";
    private static final String INFIX = "infix";
    private static final String LAST_NAME = "lastName";
    private static final String GENDER = "gender";

    @NonNull
    public static final Serializer<ShopperName> SERIALIZER = new Serializer<ShopperName>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull ShopperName modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(FIRST_NAME, modelObject.getFirstName());
                jsonObject.putOpt(INFIX, modelObject.getInfix());
                jsonObject.putOpt(LAST_NAME, modelObject.getLastName());
                jsonObject.putOpt(GENDER, modelObject.getGender());
            } catch (JSONException e) {
                throw new ModelSerializationException(ShopperName.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public ShopperName deserialize(@NonNull JSONObject jsonObject) {
            final ShopperName shopperName = new ShopperName();

            shopperName.setFirstName(jsonObject.optString(FIRST_NAME, null));
            shopperName.setInfix(jsonObject.optString(INFIX, null));
            shopperName.setLastName(jsonObject.optString(LAST_NAME, null));
            shopperName.setGender(jsonObject.optString(GENDER, null));

            return shopperName;
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    private String firstName;
    private String infix;
    private String lastName;
    private String gender;

    @NonNull
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NonNull String firstName) {
        this.firstName = firstName;
    }

    @NonNull
    public String getInfix() {
        return infix;
    }

    public void setInfix(@NonNull String infix) {
        this.infix = infix;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@NonNull String lastName) {
        this.lastName = lastName;
    }

    @NonNull
    public String getGender() {
        return gender;
    }

    public void setGender(@NonNull String gender) {
        this.gender = gender;
    }
}
