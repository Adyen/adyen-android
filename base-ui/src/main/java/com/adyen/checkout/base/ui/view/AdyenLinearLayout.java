/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/9/2019.
 */

package com.adyen.checkout.base.ui.view;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.adyen.checkout.base.ComponentView;
import com.adyen.checkout.base.component.BasePaymentComponent;

public abstract class AdyenLinearLayout<ComponentT extends BasePaymentComponent> extends LinearLayout implements
        ComponentView<ComponentT> {

    private ComponentT mComponent;

    public AdyenLinearLayout(@NonNull Context context) {
        super(context);
    }

    public AdyenLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdyenLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(isInEditMode() ? VISIBLE : GONE);
    }

    @Override
    public void attach(@NonNull ComponentT component, @NonNull LifecycleOwner lifecycleOwner) {
        this.mComponent = component;

        this.onComponentAttached();
        this.initView();
        setVisibility(VISIBLE);
        mComponent.sendAnalyticsEvent(getContext());
        this.observeComponentChanges(lifecycleOwner);
    }

    @NonNull
    protected ComponentT getComponent() {
        if (mComponent == null) {
            throw new RuntimeException("Should not get Component before it's attached");
        }
        return mComponent;
    }

    /**
     * This function will be called after the component got attached and the view got initialized.
     * It's better to Observer on live data objects here.
     */
    protected abstract void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner);
}
