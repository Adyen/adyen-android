/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.ideal;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.issuerlist.IssuerListSpinnerView;

public final class IdealSpinnerView extends IssuerListSpinnerView<IdealComponent> {
    private static final String TAG = LogUtil.getTag();

    public IdealSpinnerView(@NonNull Context context) {
        super(context);
    }

    public IdealSpinnerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IdealSpinnerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void attach(@NonNull IdealComponent component, @NonNull LifecycleOwner lifecycleOwner) {
        super.attach(component, lifecycleOwner);
        Logger.d(TAG, "attach");
        mComponent.observeOutputData(lifecycleOwner, createOutputDataObserver());
    }
}
