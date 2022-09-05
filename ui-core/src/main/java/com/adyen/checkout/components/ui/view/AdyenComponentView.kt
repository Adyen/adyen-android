/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2022.
 */
package com.adyen.checkout.components.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.extensions.createLocalizedContext
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.ViewProvidingComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AdyenComponentView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var component: ViewableComponent<*, *, *>
    private lateinit var componentView: ComponentViewNew

    init {
        isVisible = isInEditMode
    }

    fun attach(
        component: ViewableComponent<*, *, *>,
        lifecycleOwner: LifecycleOwner
    ) {
        this.component = component

        // TODO remove when all components are supported
        if (component !is ViewProvidingComponent) throw IllegalArgumentException("Not implemented yet")

        component.viewFlow
            .onEach { loadView(it, lifecycleOwner.lifecycleScope) }
            .launchIn(lifecycleOwner.lifecycleScope)

        isVisible = true
        component.sendAnalyticsEvent(context)
    }

    // TODO how does rotation affect this? when views are in the xml layout file their state gets automatically saved
    //  but not with addView
    private fun loadView(viewType: ComponentViewType?, coroutineScope: CoroutineScope) {
        removeAllViews()
        viewType ?: return

        val component = component
        if (component !is ViewProvidingComponent) throw IllegalArgumentException("Not implemented yet")

        val componentView = component.getView(viewType, context, attrs, defStyleAttr)
        this.componentView = componentView

        val configuration = component.configuration
        val localizedContext = context.createLocalizedContext(configuration.shopperLocale)

        addView(componentView.getView())

        componentView.initView(component.delegate, coroutineScope, localizedContext)
    }

    val isConfirmationRequired
        get() = componentView.isConfirmationRequired

    fun highlightValidationErrors() {
        componentView.highlightValidationErrors()
    }
}
