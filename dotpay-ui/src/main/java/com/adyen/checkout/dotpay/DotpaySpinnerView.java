/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */

package com.adyen.checkout.dotpay;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.adyen.checkout.base.model.payments.request.DotpayPaymentMethod;
import com.adyen.checkout.issuerlist.IssuerListSpinnerView;

public final class DotpaySpinnerView extends IssuerListSpinnerView<DotpayPaymentMethod, DotpayComponent> {

    public DotpaySpinnerView(@NonNull Context context) {
        super(context);
    }

    public DotpaySpinnerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DotpaySpinnerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
