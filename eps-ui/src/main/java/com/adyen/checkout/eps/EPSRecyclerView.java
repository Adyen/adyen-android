/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.eps;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.adyen.checkout.base.model.payments.request.EPSPaymentMethod;
import com.adyen.checkout.issuerlist.IssuerListRecyclerView;

@SuppressWarnings("AbbreviationAsWordInName")
public class EPSRecyclerView extends IssuerListRecyclerView<EPSPaymentMethod, EPSComponent> {

    public EPSRecyclerView(@NonNull Context context) {
        super(context);
    }

    public EPSRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EPSRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean hideIssuersLogo() {
        return true;
    }
}
