/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/5/2019.
 */

package com.adyen.checkout.ideal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.adyen.checkout.base.model.payments.request.IdealPaymentMethod;
import com.adyen.checkout.issuerlist.IssuerListRecyclerView;

public class IdealRecyclerView extends IssuerListRecyclerView<IdealPaymentMethod, IdealComponent> {

    public IdealRecyclerView(@NonNull Context context) {
        super(context);
    }

    public IdealRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IdealRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
