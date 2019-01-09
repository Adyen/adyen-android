/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/10/2018.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.base.internal.JsonSerializable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class PersonalDetails implements Parcelable, JsonSerializable {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @NonNull
    public static final Creator<PersonalDetails> CREATOR = new Creator<PersonalDetails>() {
        @Override
        public PersonalDetails createFromParcel(@NonNull Parcel parcel) {
            return new PersonalDetails(parcel);
        }

        @Override
        public PersonalDetails[] newArray(int size) {
            return new PersonalDetails[size];
        }
    };

    @NonNull
    public static final String KEY_FIRST_NAME = "firstName";
    @NonNull
    public static final String KEY_LAST_NAME = "lastName";
    @NonNull
    public static final String KEY_GENDER = "gender";
    @NonNull
    public static final String KEY_DATE_OF_BIRTH = "dateOfBirth";
    @NonNull
    public static final String KEY_TELEPHONE_NUMBER = "telephoneNumber";
    @NonNull
    public static final String KEY_SOCIAL_SECURITY_NUMBER = "socialSecurityNumber";
    @NonNull
    public static final String KEY_SHOPPER_EMAIL = "shopperEmail";

    private String mFirstName;
    private String mGender;
    private String mLastName;
    private String mDateOfBirth;
    private String mTelephoneNumber;
    private String mSocialSecurityNumber;
    private String mShopperEmail;


    private PersonalDetails() {
        // Empty constructor for Builder.
    }

    private PersonalDetails(@NonNull Parcel in) {
        mFirstName = in.readString();
        mLastName = in.readString();
        mGender = in.readString();
        mDateOfBirth = in.readString();
        mTelephoneNumber = in.readString();
        mSocialSecurityNumber = in.readString();
        mShopperEmail = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(mFirstName);
        parcel.writeString(mLastName);
        parcel.writeString(mGender);
        parcel.writeString(mDateOfBirth);
        parcel.writeString(mTelephoneNumber);
        parcel.writeString(mSocialSecurityNumber);
        parcel.writeString(mShopperEmail);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_FIRST_NAME, mFirstName);
        jsonObject.put(KEY_LAST_NAME, mLastName);
        jsonObject.put(KEY_GENDER, mGender);
        jsonObject.put(KEY_DATE_OF_BIRTH, mDateOfBirth);
        jsonObject.put(KEY_TELEPHONE_NUMBER, mTelephoneNumber);
        jsonObject.put(KEY_SOCIAL_SECURITY_NUMBER, mSocialSecurityNumber);
        jsonObject.put(KEY_SHOPPER_EMAIL, mShopperEmail);

        return jsonObject;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersonalDetails that = (PersonalDetails) o;

        if (mFirstName != null ? !mFirstName.equals(that.mFirstName) : that.mFirstName != null) {
            return false;
        }
        if (mGender != null ? !mGender.equals(that.mGender) : that.mGender != null) {
            return false;
        }
        if (mLastName != null ? !mLastName.equals(that.mLastName) : that.mLastName != null) {
            return false;
        }
        if (mDateOfBirth != null ? !mDateOfBirth.equals(that.mDateOfBirth) : that.mDateOfBirth != null) {
            return false;
        }
        if (mTelephoneNumber != null ? !mTelephoneNumber.equals(that.mTelephoneNumber) : that.mTelephoneNumber != null) {
            return false;
        }
        if (mSocialSecurityNumber != null ? !mSocialSecurityNumber.equals(that.mSocialSecurityNumber) : that.mSocialSecurityNumber != null) {
            return false;
        }
        return mShopperEmail != null ? mShopperEmail.equals(that.mShopperEmail) : that.mShopperEmail == null;
    }

    @Override
    public int hashCode() {
        int result = mFirstName != null ? mFirstName.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mGender != null ? mGender.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mLastName != null ? mLastName.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mDateOfBirth != null ? mDateOfBirth.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mTelephoneNumber != null ? mTelephoneNumber.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mSocialSecurityNumber != null ? mSocialSecurityNumber.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mShopperEmail != null ? mShopperEmail.hashCode() : 0);
        return result;
    }

    public static final class Builder {
        private PersonalDetails mPersonalDetails;

        public Builder(@NonNull String firstName, @NonNull String lastName, @NonNull String telephoneNumber, @NonNull String shopperEmail) {
            mPersonalDetails = new PersonalDetails();

            mPersonalDetails.mFirstName = firstName;
            mPersonalDetails.mLastName = lastName;
            mPersonalDetails.mTelephoneNumber = telephoneNumber;
            mPersonalDetails.mShopperEmail = shopperEmail;
        }

        @NonNull
        public PersonalDetails.Builder setDateOfBirth(@Nullable Date dateOfBirth) {
            if (dateOfBirth != null) {
                DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
                mPersonalDetails.mDateOfBirth = dateFormat.format(dateOfBirth);
            }
            return this;
        }

        @NonNull
        public PersonalDetails.Builder setGender(@Nullable String gender) {
            mPersonalDetails.mGender = gender;
            return this;
        }

        @NonNull
        public PersonalDetails.Builder setSocialSecurityNumber(@Nullable String socialSecurityNumber) {
            mPersonalDetails.mSocialSecurityNumber = socialSecurityNumber;
            return this;
        }

        @NonNull
        public PersonalDetails build() {
            return mPersonalDetails;
        }
    }
}
