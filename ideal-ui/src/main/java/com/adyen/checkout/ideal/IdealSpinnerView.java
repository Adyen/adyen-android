/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.ideal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.adyen.checkout.base.model.payments.request.IdealPaymentMethod;
import com.adyen.checkout.issuerlist.IssuerListSpinnerView;

public final class IdealSpinnerView extends IssuerListSpinnerView<IdealPaymentMethod, IdealComponent> {

    public IdealSpinnerView(@NonNull Context context) {
        super(context);
    }

    public IdealSpinnerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IdealSpinnerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
