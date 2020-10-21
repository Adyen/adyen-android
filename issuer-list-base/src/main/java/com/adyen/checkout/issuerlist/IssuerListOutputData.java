/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.OutputData;

public class IssuerListOutputData implements OutputData {

    private IssuerModel mSelectedIssuer;
    private boolean mIsValid;

    public IssuerListOutputData(@Nullable IssuerModel selectedIssuer) {
        setSelectedIssuer(selectedIssuer);
    }

    @Nullable
    public IssuerModel getSelectedIssuer() {
        return mSelectedIssuer;
    }

    private void setSelectedIssuer(@Nullable IssuerModel selectedIssuer) {
        mSelectedIssuer = selectedIssuer;
        mIsValid = mSelectedIssuer != null;
    }

    @Override
    public boolean isValid() {
        return mIsValid;
    }
}
