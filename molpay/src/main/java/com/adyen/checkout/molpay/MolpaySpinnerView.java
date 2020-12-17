/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */

package com.adyen.checkout.molpay;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.model.payments.request.MolpayPaymentMethod;
import com.adyen.checkout.issuerlist.IssuerListSpinnerView;

public final class MolpaySpinnerView extends IssuerListSpinnerView<MolpayPaymentMethod, MolpayComponent> {

    public MolpaySpinnerView(@NonNull Context context) {
        super(context);
    }

    public MolpaySpinnerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MolpaySpinnerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
