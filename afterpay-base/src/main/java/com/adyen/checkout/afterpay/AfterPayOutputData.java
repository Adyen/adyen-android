/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/12/2019.
 */

package com.adyen.checkout.afterpay;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.OutputData;

class AfterPayOutputData implements OutputData {

    private AfterPayPersonalDataOutputData mAfterPayPersonalDataOutputData;
    private AfterPayAddressOutputData mBillingAddressOutputData;
    private AfterPayAddressOutputData mDeliveryAddressOutputData;
    private boolean mSeparateDeliveryAddress;
    private boolean mAgreementChecked;

    @NonNull
    public AfterPayPersonalDataOutputData getAfterPayPersonalDataOutputData() {
        return mAfterPayPersonalDataOutputData;
    }

    public void setAfterPayPersonalDataOutputData(@NonNull AfterPayPersonalDataOutputData afterPayPersonalDataOutputData) {
        mAfterPayPersonalDataOutputData = afterPayPersonalDataOutputData;
    }

    @Override
    public boolean isValid() {
        return mAgreementChecked && mAfterPayPersonalDataOutputData.isValid() && mBillingAddressOutputData.isValid()
                && mDeliveryAddressOutputData.isValid();
    }

    @NonNull
    public AfterPayAddressOutputData getBillingAddressOutputData() {
        return mBillingAddressOutputData;
    }

    public void setBillingAddressOutputData(@NonNull AfterPayAddressOutputData billingAddressOutputData) {
        mBillingAddressOutputData = billingAddressOutputData;
    }

    @NonNull
    public AfterPayAddressOutputData getDeliveryAddressOutputData() {
        return mDeliveryAddressOutputData;
    }

    public void setDeliveryAddressOutputData(@NonNull AfterPayAddressOutputData deliveryAddressOutputData) {
        mDeliveryAddressOutputData = deliveryAddressOutputData;
    }

    @NonNull
    public boolean isAgreementChecked() {
        return mAgreementChecked;
    }

    public void setAgreementChecked(@NonNull boolean agreementChecked) {
        mAgreementChecked = agreementChecked;
    }

    public void setSeparateDeliveryAddress(@NonNull boolean separateDeliveryAddress) {
        mSeparateDeliveryAddress = separateDeliveryAddress;
    }

    @NonNull
    public boolean isSeparateDeliveryAddress() {
        return mSeparateDeliveryAddress;
    }
}
