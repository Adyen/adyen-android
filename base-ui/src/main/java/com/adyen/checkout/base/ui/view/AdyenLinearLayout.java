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
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.adyen.checkout.base.ComponentView;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.Locale;

public abstract class AdyenLinearLayout<ComponentT extends BasePaymentComponent> extends LinearLayout implements ComponentView<ComponentT> {
    private static final String TAG = LogUtil.getTag();

    private ComponentT mComponent;

    @NonNull
    protected Context mLocalizedContext;

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
        mComponent = component;

        onComponentAttached();
        initLocalization(mComponent.getConfiguration().getShopperLocale());
        initView();
        initLocalizedStrings(mLocalizedContext);
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

    private void initLocalization(@NonNull Locale shopperLocale) {

        // We need to get the strings from the styles instead of the strings.xml because merchants can override them.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final Configuration configuration = getContext().getResources().getConfiguration();
            final Configuration newConfig = new Configuration(configuration);
            newConfig.setLocale(shopperLocale);
            mLocalizedContext = getContext().createConfigurationContext(newConfig);
        } else {
            mLocalizedContext = getContext();
            Logger.e(TAG, "Cannot load custom localized strings bellow API 17. Falling back to user device Locale.");
        }
    }

    /**
     * Set the view Strings based on the localized context.
     * @param localizedContext A configuration context with the Locale from the Component Configuration.
     */
    protected abstract void initLocalizedStrings(@NonNull Context localizedContext);

    /**
     * This function will be called after the component got attached and the view got initialized.
     * It's better to Observer on live data objects here.
     */
    protected abstract void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner);
}
