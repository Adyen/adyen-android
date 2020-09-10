/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.InputData;

public class IssuerListInputData implements InputData {

    private IssuerModel mSelectedIssuer;

    void setSelectedIssuer(@Nullable IssuerModel selectedIssuer) {
        mSelectedIssuer = selectedIssuer;
    }

    @Nullable
    IssuerModel getSelectedIssuer() {
        return mSelectedIssuer;
    }
}
