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
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.components.Component
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.extensions.createLocalizedContext
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.PaymentComponentUIEvent
import com.adyen.checkout.components.ui.PaymentComponentUIState
import com.adyen.checkout.components.ui.R
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.databinding.AdyenComponentViewBinding
import com.adyen.checkout.components.util.CurrencyUtils
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

    private val binding: AdyenComponentViewBinding = AdyenComponentViewBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    private var componentView: ComponentView? = null
    private var componentViewType: ComponentViewType? = null

    init {
        isVisible = isInEditMode
        orientation = VERTICAL
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
                binding.frameLayoutComponentContainer.removeAllViews()

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
        this.componentViewType = viewType

        val localizedContext = context.createLocalizedContext(componentParams.shopperLocale)

        binding.payButton.setText(viewType, componentParams, localizedContext)

        val view = componentView.getView()
        binding.frameLayoutComponentContainer.addView(view)
        view.updateLayoutParams { width = LayoutParams.MATCH_PARENT }

        componentView.initView(delegate, coroutineScope, localizedContext)

        if ((delegate as? ButtonDelegate)?.isConfirmationRequired() == true) {
            val uiStateDelegate = (delegate as? UIStateDelegate)
            uiStateDelegate?.uiStateFlow?.onEach {
                // TODO check if setPaymentPendingInitialization has to be called on each event?
                when (it) {
                    PaymentComponentUIState.Idle -> setPaymentPendingInitialization(false)
                    PaymentComponentUIState.Loading -> setPaymentPendingInitialization(true)
                }
            }?.launchIn(coroutineScope)

            uiStateDelegate?.uiEventFlow?.onEach {
                when (it) {
                    PaymentComponentUIEvent.InvalidUI -> highlightValidationErrors()
                }
            }?.launchIn(coroutineScope)

            binding.payButton.isVisible = true
            binding.payButton.setOnClickListener {
                (delegate as? ButtonDelegate)?.onSubmit()
            }
        } else {
            binding.payButton.isVisible = false
            binding.payButton.setOnClickListener(null)
        }
    }

    private fun setPaymentPendingInitialization(pending: Boolean) {
        binding.payButton.isVisible = !pending
        if (pending) binding.progressBar.show() else binding.progressBar.hide()
    }

    private fun Button.setText(
        viewType: ComponentViewType,
        componentParams: ComponentParams,
        localizedContext: Context
    ) {
        if (viewType is AmountButtonComponentViewType && !componentParams.amount.isEmpty) {
            text = if (componentParams.amount.isZero) {
                localizedContext.getString(R.string.confirm_preauthorization)
            } else {
                localizedContext.getString(
                    R.string.pay_button_with_value,
                    CurrencyUtils.formatAmount(componentParams.amount, componentParams.shopperLocale)
                )
            }
        } else if (viewType is ButtonComponentViewType) {
            text = localizedContext.getString(viewType.buttonTextResId)
        }
    }

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
