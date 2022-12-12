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
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.components.Component
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.extensions.createLocalizedContext
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A View that can display input and fill in details for a Component.
 */
class AdyenComponentView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr) {

    private var componentView: ComponentView? = null

    init {
        isVisible = isInEditMode
    }

    /**
     * Attach this view to a component to interact with.
     *
     * @param component      The component.
     * @param lifecycleOwner The lifecycle owner where the view is.
     */
    fun <T> attach(
        component: T,
        lifecycleOwner: LifecycleOwner
    ) where T : ViewableComponent, T : Component {
        component.viewFlow
            .onEach { componentViewType ->
                removeAllViews()

                if (componentViewType == null) {
                    Logger.i(TAG, "Component view type is null, ignoring.")
                    return@onEach
                }

                val delegate = component.delegate
                if (delegate !is ViewProvidingDelegate) {
                    Logger.i(TAG, "View attached to non viewable component, ignoring.")
                    return@onEach
                }

                loadView(
                    viewType = componentViewType,
                    delegate = delegate,
                    componentParams = delegate.componentParams,
                    coroutineScope = lifecycleOwner.lifecycleScope,
                )
            }
            .launchIn(lifecycleOwner.lifecycleScope)

        isVisible = true
    }

    private fun loadView(
        viewType: ComponentViewType,
        delegate: ComponentDelegate,
        componentParams: ComponentParams,
        coroutineScope: CoroutineScope,
    ) {
        val componentView = viewType.viewProvider.getView(viewType, context, attrs, defStyleAttr)
        this.componentView = componentView

        val localizedContext = context.createLocalizedContext(componentParams.shopperLocale)

        val view = componentView.getView()
        addView(view)
        view.updateLayoutParams { width = LayoutParams.MATCH_PARENT }

        componentView.initView(delegate, coroutineScope, localizedContext)
    }

    /**
     * Tells if the view interaction requires confirmation from the user to start the payment flow.
     * Confirmation usually is obtained by a "Pay" button the user need to press to start processing the payment.
     * If confirmation is not required, it means the view handles input in a way that the user has already expressed the
     * desire to continue.
     *
     * Each type of view always returns the same value, so if the type of view is known, there is no need to check this
     * method.
     *
     * @return If an update from the component attached to this View requires further user confirmation to continue or
     * not.
     */
    val isConfirmationRequired: Boolean
        get() = componentView?.isConfirmationRequired ?: false

    /**
     * Highlight and focus on the current validation errors for the user to take action.
     * If the component doesn't need validation or if everything is already valid, nothing will happen.
     */
    fun highlightValidationErrors() {
        componentView?.highlightValidationErrors()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
