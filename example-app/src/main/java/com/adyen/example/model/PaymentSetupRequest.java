/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 08/08/2017.
 */

package com.adyen.example.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.example.model.request.Address;
import com.adyen.example.model.request.Amount;
import com.adyen.example.model.request.Configuration;
import com.adyen.example.model.request.LineItem;
import com.adyen.example.model.request.ShopperName;
import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.List;

public final class PaymentSetupRequest implements Serializable {
    @Json(name = "merchantAccount")
    private String mMerchantAccount;

    @Json(name = "shopperLocale")
    private String mShopperLocale;

    @Json(name = "token")
    private String mToken;

    @Json(name = "returnUrl")
    private String mReturnUrl;

    @Json(name = "countryCode")
    private String mCountryCode;

    @Json(name = "amount")
    private Amount mAmount;

    @Json(name = "channel")
    private String mChannel = "Android";

    @Json(name = "reference")
    private String mReference;

    @Json(name = "shopperReference")
    private String mShopperReference;

    @Json(name = "shopperEmail")
    private String mShopperEmail;

    @Json(name = "configuration")
    private Configuration mConfiguration;

    @Json(name = "lineItems")
    private List<LineItem> mLineItems;

    @Json(name = "shopperName")
    private ShopperName mShopperName;

    @Json(name = "dateOfBirth")
    private String mDateOfBirth;

    @Json(name = "socialSecurityNumber")
    private String mSocialSecurityNumber;

    @Json(name = "telephoneNumber")
    private String mTelephoneNumber;

    @Json(name = "billingAddress")
    private Address mBillingAddress;

    @Json(name = "deliveryAddress")
    private Address mDeliveryAddress;

    public static final class Builder {
        private final PaymentSetupRequest mPaymentSetupRequest;

        public Builder(@NonNull String merchantAccount, @NonNull String token, @NonNull String returnUrl, @NonNull Amount amount) {
            mPaymentSetupRequest = new PaymentSetupRequest();
            mPaymentSetupRequest.mMerchantAccount = merchantAccount;
            mPaymentSetupRequest.mToken = token;
            mPaymentSetupRequest.mReturnUrl = returnUrl;
            mPaymentSetupRequest.mAmount = amount;
        }

        @NonNull
        public Builder setShopperLocale(@NonNull String shopperLocale) {
            mPaymentSetupRequest.mShopperLocale = shopperLocale;
            return this;
        }

        @NonNull
        public Builder setCountryCode(@NonNull String countryCode) {
            mPaymentSetupRequest.mCountryCode = countryCode;
            return this;
        }

        @NonNull
        public Builder setReference(@NonNull String reference) {
            mPaymentSetupRequest.mReference = reference;
            return this;
        }

        @NonNull
        public Builder setShopperReference(@NonNull String shopperReference) {
            mPaymentSetupRequest.mShopperReference = shopperReference;
            return this;
        }

        @NonNull
        public Builder setShopperEmail(@Nullable String shopperEmail) {
            mPaymentSetupRequest.mShopperEmail = shopperEmail;
            return this;
        }

        @NonNull
        public Builder setConfiguration(@Nullable Configuration configuration) {
            mPaymentSetupRequest.mConfiguration = configuration;
            return this;
        }

        public Builder setLineItems(@Nullable List<LineItem> lineItems) {
            mPaymentSetupRequest.mLineItems = lineItems;
            return this;
        }

        @NonNull
        public Builder setDateOfBirth(@Nullable String dateOfBirth) {
            mPaymentSetupRequest.mDateOfBirth = dateOfBirth;
            return this;
        }

        @NonNull
        public Builder setTelephoneNumber(@Nullable String telephoneNumber) {
            mPaymentSetupRequest.mTelephoneNumber = telephoneNumber;
            return this;
        }

        @NonNull
        public Builder setSocialSecurityNumber(@Nullable String socialSecurityNumber) {
            mPaymentSetupRequest.mSocialSecurityNumber = socialSecurityNumber;
            return this;
        }

        public Builder setShopperName(@Nullable ShopperName shopperName) {
            mPaymentSetupRequest.mShopperName = shopperName;
            return this;
        }

        public Builder setBillingAddress(@Nullable Address billingAddress) {
            mPaymentSetupRequest.mBillingAddress = billingAddress;
            return this;
        }

        public Builder setDeliveryAddress(@Nullable Address deliveryAddress) {
            mPaymentSetupRequest.mDeliveryAddress = deliveryAddress;
            return this;
        }

        @NonNull
        public PaymentSetupRequest build() {
            return mPaymentSetupRequest;
        }
    }

}
