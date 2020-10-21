/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/12/2019.
 */

package com.adyen.checkout.afterpay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.InputData;

public class AfterPayInputData implements InputData {

    private AfterPayPersonalDataInputData mPersonalDataInputData;
    private AfterPayAddressInputData mBillingAddressInputData;
    private AfterPayAddressInputData mDeliveryAddressInputData;
    private boolean mSeparateDeliveryAddress;
    private boolean mAgreementChecked;

    @Nullable
    public AfterPayPersonalDataInputData getPersonalDataInputData() {
        return mPersonalDataInputData;
    }

    public void setPersonalDataInputData(@NonNull AfterPayPersonalDataInputData personalDataInputData) {
        mPersonalDataInputData = personalDataInputData;
    }

    @Nullable
    public AfterPayAddressInputData getBillingAddressInputData() {
        return mBillingAddressInputData;
    }

    public void setBillingAddressInputData(@Nullable AfterPayAddressInputData billingAddressInputData) {
        mBillingAddressInputData = billingAddressInputData;
    }

    @Nullable
    public AfterPayAddressInputData getDeliveryAddressInputData() {
        return mDeliveryAddressInputData;
    }

    public void setDeliveryAddressInputData(@Nullable AfterPayAddressInputData deliveryAddressInputData) {
        mDeliveryAddressInputData = deliveryAddressInputData;
    }

    public boolean isSeparateDeliveryAddressEnable() {
        return mSeparateDeliveryAddress;
    }

    public void setSeparateDeliveryAddress(boolean separateDeliveryAddress) {
        mSeparateDeliveryAddress = separateDeliveryAddress;
    }

    public boolean isAgreementChecked() {
        return mAgreementChecked;
    }

    public void setAgreementChecked(boolean agreementChecked) {
        mAgreementChecked = agreementChecked;
    }
}
