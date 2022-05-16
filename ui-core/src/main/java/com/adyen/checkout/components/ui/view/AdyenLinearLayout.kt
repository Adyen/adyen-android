/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/9/2019.
 */
package com.adyen.checkout.components.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.ComponentView
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.extensions.createLocalizedContext
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.google.android.material.textfield.TextInputLayout

abstract class AdyenLinearLayout<
    OutputDataT : OutputData,
    ConfigurationT : Configuration,
    ComponentStateT,
    ComponentT : ViewableComponent<OutputDataT, ConfigurationT, ComponentStateT>>
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr),
    ComponentView<OutputDataT, ComponentT> {

    private var _component: ComponentT? = null
    protected val component: ComponentT
        get() { return _component ?: throw IllegalStateException("Should not get Component before it's attached") }

    protected lateinit var localizedContext: Context

    init {
        isVisible = isInEditMode
    }

    override fun attach(component: ComponentT, lifecycleOwner: LifecycleOwner) {
        _component = component
        onComponentAttached()
        localizedContext = context.createLocalizedContext(component.configuration.shopperLocale)
        initView()
        initLocalizedStrings(localizedContext)
        isVisible = true
        component.sendAnalyticsEvent(context)
        observeComponentChanges(lifecycleOwner)
    }

    /**
     * Set the view Strings based on the localized context.
     * @param localizedContext A configuration context with the Locale from the Component Configuration.
     */
    protected abstract fun initLocalizedStrings(localizedContext: Context)

    /**
     * This function will be called after the component got attached and the view got initialized.
     * It's better to Observer on live data objects here.
     */
    protected abstract fun observeComponentChanges(lifecycleOwner: LifecycleOwner)

    protected fun TextInputLayout.setLocalizedHintFromStyle(@StyleRes styleResId: Int) {
        setLocalizedHintFromStyle(styleResId, localizedContext)
    }

    protected fun TextView.setLocalizedTextFromStyle(@StyleRes styleResId: Int) {
        setLocalizedTextFromStyle(styleResId, localizedContext)
    }
}
