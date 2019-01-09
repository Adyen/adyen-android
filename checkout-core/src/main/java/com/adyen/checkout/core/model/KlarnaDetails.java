/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/12/2018.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * {@link PaymentMethodDetails} for Klarna.
 * This is an unmodified version of an {@link OpenInvoiceDetails}.
 */
public final class KlarnaDetails extends OpenInvoiceDetails {
    @NonNull
    public static final Creator<KlarnaDetails> CREATOR = new Creator<KlarnaDetails>() {
        @Override
        public KlarnaDetails createFromParcel(@NonNull Parcel parcel) {
            return new KlarnaDetails(parcel);
        }

        @Override
        public KlarnaDetails[] newArray(int size) {
            return new KlarnaDetails[size];
        }
    };

    private KlarnaDetails() {
        // Empty constructor for Builder.
    }

    private KlarnaDetails(@NonNull Parcel in) {
        super(in);
    }

    public static final class Builder {
        private KlarnaDetails mKlarnaDetails;

        public Builder(@NonNull PersonalDetails personalDetails, @NonNull Address billingAddress) {
            mKlarnaDetails = new KlarnaDetails();

            mKlarnaDetails.mPersonalDetails = personalDetails;
            mKlarnaDetails.mBillingAddress = billingAddress;
        }

        @NonNull
        public KlarnaDetails.Builder setSeparateDeliveryAddress(@Nullable Boolean separateDeliveryAddress) {
            mKlarnaDetails.mSeparateDeliveryAddress = separateDeliveryAddress;
            return this;
        }

        @NonNull
        public KlarnaDetails.Builder setDeliveryAddress(@Nullable Address deliveryAddress) {
            mKlarnaDetails.mDeliveryAddress = deliveryAddress;
            return this;
        }

        @NonNull
        public KlarnaDetails.Builder setConsentCheckbox(@Nullable Boolean consentCheckbox) {
            mKlarnaDetails.mConsentCheckbox = consentCheckbox;
            return this;
        }

        @NonNull
        public KlarnaDetails build() {
            return mKlarnaDetails;
        }
    }
}
