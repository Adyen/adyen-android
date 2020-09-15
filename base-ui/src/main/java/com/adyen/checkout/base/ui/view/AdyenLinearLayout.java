/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/9/2019.
 */

package com.adyen.checkout.base.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.adyen.checkout.base.ComponentView;
import com.adyen.checkout.base.ViewableComponent;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.OutputData;

import java.util.Locale;

public abstract class AdyenLinearLayout<
        OutputDataT extends OutputData,
        ConfigurationT extends Configuration,
        ComponentStateT,
        ComponentT extends ViewableComponent<OutputDataT, ConfigurationT, ComponentStateT>>
        extends LinearLayout implements ComponentView<OutputDataT, ComponentT> {

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
        final android.content.res.Configuration configuration = getContext().getResources().getConfiguration();
        final android.content.res.Configuration newConfig = new android.content.res.Configuration(configuration);
        newConfig.setLocale(shopperLocale);
        mLocalizedContext = getContext().createConfigurationContext(newConfig);
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
