/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */

package com.adyen.checkout.molpay;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.adyen.checkout.issuerlist.IssuerListRecyclerView;

public class MolpayRecyclerView extends IssuerListRecyclerView<MolpayComponent> {

    public MolpayRecyclerView(@NonNull Context context) {
        super(context);
    }

    public MolpayRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MolpayRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
