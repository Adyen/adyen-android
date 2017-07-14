package com.adyen.core.models.paymentdetails;


import android.support.annotation.NonNull;

import com.adyen.core.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public final class InputDetail implements Serializable {

    private String key;
    private String value;
    private Type type;
    private boolean optional = true;
    private ArrayList<Item> items = new ArrayList<>();

    private InputDetail() {

    }

    public boolean fill(String value) {
        this.value = value;
        return true;
    }

    public boolean fill(boolean value) {
        if (this.type == Type.Boolean) {
            this.value = String.valueOf(value);
            return true;
        } else {
            return false;
        }
    }

    public boolean isFilled() {
        return !StringUtils.isEmptyOrNull(value);
    }

    public static InputDetail fromJson(@NonNull final JSONObject jsonObject) throws JSONException {
        InputDetail inputDetail = new InputDetail();
        inputDetail.key = jsonObject.getString("key");
        inputDetail.optional = jsonObject.optBoolean("optional", false);
        inputDetail.type = Type.fromString(jsonObject.getString("type"));
        if (inputDetail.type == Type.Select) {
            JSONArray jsonItems = jsonObject.getJSONArray("items");
            for (int i = 0; i < jsonItems.length(); i++) {
                inputDetail.items.add(Item.fromJson(jsonItems.getJSONObject(i)));
            }
        }
        return inputDetail;
    }


    public String getKey() {
        return key;
    }

    public Type getType() {
        return type;
    }

    public boolean isOptional() {
        return optional;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public String getValue() {
        return value;
    }

    public enum Type implements Serializable {
        Text("text"), // An open text field. Validations should specify what is valid or not
        CardToken("cardToken"), // A card type object. Needs to have five fields: "holderName", "number", "expiryYear", "expiryMonth", "cvc"
        Iban("iban"), // A IBAN
        Select("select"), // A list to select from
        Boolean("boolean"), // A boolean yes/no or true/false
        ApplePayToken("applePayToken"), // A token used by a wallet
        AndroidPayToken("androidPayToken"), // A token used by a wallet
        SamsungPayToken("samsungPayToken"), // A token used by a wallet
        Cvc("cvc"), //A field to enter CVC code
        Address("Address"),
        Unknown("Unknown");

        private String apiField;

        Type(String apiField) {
            this.apiField = apiField;
        }

        public String getApiField() {
            return apiField;
        }

        static Type fromString(@NonNull final String type) {
            for (Type fieldType : Type.values()) {
                if (fieldType.getApiField().equals(type)) {
                    return fieldType;
                }
            }
            return Unknown;
        }

    }

    public static final class Item implements Serializable {
        private String id;
        private String imageUrl;
        private String name;

        private Item() {

        }

        static Item fromJson(@NonNull final JSONObject jsonObject) throws JSONException {
            Item item = new Item();
            item.id = jsonObject.getString("id");
            item.imageUrl = jsonObject.getString("imageUrl");
            item.name = jsonObject.getString("name");
            return item;
        }

        public String getName() {
            return name;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getId() {
            return id;
        }
    }

}
