/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.entercash;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.adyen.checkout.base.model.payments.request.EntercashPaymentMethod;
import com.adyen.checkout.issuerlist.IssuerListRecyclerView;

public class EntercashRecyclerView extends IssuerListRecyclerView<EntercashPaymentMethod, EntercashComponent> {

    public EntercashRecyclerView(@NonNull Context context) {
        super(context);
    }

    public EntercashRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EntercashRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
