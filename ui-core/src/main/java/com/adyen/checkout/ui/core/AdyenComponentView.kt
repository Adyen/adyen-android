/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/2/2023.
 */
package com.adyen.checkout.ui.core

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.components.core.internal.Component
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.util.createLocalizedContext
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.databinding.AdyenComponentViewBinding
import com.adyen.checkout.ui.core.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import com.adyen.checkout.ui.core.internal.ui.view.PayButton
import com.adyen.checkout.ui.core.internal.util.PayButtonFormatter
import com.adyen.checkout.ui.core.internal.util.hideKeyboard
import com.adyen.checkout.ui.core.internal.util.resetFocus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.ref.WeakReference

/**
 * A View that can display input and fill in details for a Component.
 * Declare this view in your xml layout file and call [attach] to bind it to a component.
 */
class AdyenComponentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr) {

    private val binding: AdyenComponentViewBinding = AdyenComponentViewBinding.inflate(
        LayoutInflater.from(context),
        this,
    )

    /**
     * Indicates if user interaction is blocked.
     */
    @Volatile
    private var isInteractionBlocked = false

    private var componentView: ComponentView? = null

    private var attachedComponent = WeakReference<Component?>(null)

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
        if (component == attachedComponent.get()) return

        attachedComponent = WeakReference(component)

        component.viewFlow
            .onEach { componentViewType ->
                binding.frameLayoutComponentContainer.removeAllViews()

                if (componentViewType == null) {
                    adyenLog(AdyenLogLevel.INFO) { "Component view type is null, ignoring." }
                    return@onEach
                }

                val delegate = component.delegate
                if (delegate !is ViewProvidingDelegate) {
                    adyenLog(AdyenLogLevel.INFO) { "View attached to non viewable component, ignoring." }
                    return@onEach
                }

                loadView(
                    viewType = componentViewType,
                    delegate = delegate,
                    componentParams = delegate.componentParams,
                    coroutineScope = lifecycleOwner.lifecycleScope,
                )
            }
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .launchIn(lifecycleOwner.lifecycleScope)
        isVisible = true
    }

    private fun loadView(
        viewType: ComponentViewType,
        delegate: ComponentDelegate,
        componentParams: ComponentParams,
        coroutineScope: CoroutineScope,
    ) {
        val layoutInflater = getLayoutInflater()
        val componentView = viewType.viewProvider.getView(viewType, layoutInflater)
        this.componentView = componentView

        val localizedContext = context.createLocalizedContext(componentParams.shopperLocale)

        binding.frameLayoutComponentContainer.addView(componentView.getView())
        componentView.initView(delegate, coroutineScope, localizedContext)

        val buttonDelegate = (delegate as? ButtonDelegate)
        if (buttonDelegate?.isConfirmationRequired() == true) {
            val uiStateDelegate = (delegate as? UIStateDelegate)
            uiStateDelegate?.uiStateFlow?.onEach {
                setInteractionBlocked(it.isInteractionBlocked())
            }?.launchIn(coroutineScope)

            uiStateDelegate?.uiEventFlow?.onEach {
                when (it) {
                    PaymentComponentUIEvent.InvalidUI -> highlightValidationErrors()
                }
            }?.launchIn(coroutineScope)

            binding.frameLayoutButtonContainer.isVisible = buttonDelegate.shouldShowSubmitButton()
            val buttonView = (viewType as ButtonComponentViewType)
                .buttonViewProvider.getButton(context)
            buttonView.initialize(buttonDelegate, coroutineScope)
            buttonView.setText(viewType, componentParams, localizedContext)
            buttonView.setOnClickListener {
                buttonDelegate.onSubmit()
            }
            binding.frameLayoutButtonContainer.addView(buttonView)
        } else {
            binding.frameLayoutButtonContainer.isVisible = false
        }
    }

    /**
     * Returns the [LayoutInflater] of the parent activity or fragment. Using `LayoutInflater.from(context)` when the
     * view's parent is a fragment will return the [LayoutInflater] of the fragment's activity. This causes issues with
     * nested fragment's.
     */
    @Suppress("SwallowedException")
    private fun getLayoutInflater(): LayoutInflater = try {
        findFragment<Fragment>().layoutInflater
    } catch (e: IllegalStateException) {
        LayoutInflater.from(context)
    }

    private fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        this.isInteractionBlocked = isInteractionBlocked

        binding.frameLayoutButtonContainer.children.forEach { it.isEnabled = !isInteractionBlocked }

        if (isInteractionBlocked) {
            resetFocus()
            hideKeyboard()
        }
    }

    private fun PayButton.setText(
        viewType: ComponentViewType,
        componentParams: ComponentParams,
        localizedContext: Context
    ) {
        val text = when (viewType) {
            is AmountButtonComponentViewType -> {
                PayButtonFormatter.getPayButtonText(
                    amount = componentParams.amount,
                    locale = componentParams.shopperLocale,
                    localizedContext = localizedContext,
                    emptyAmountStringResId = viewType.buttonTextResId,
                )
            }

            is ButtonComponentViewType -> {
                localizedContext.getString(viewType.buttonTextResId)
            }

            else -> {
                null
            }
        }
        setText(text)
    }

    /**
     * Highlight and focus on the current validation errors for the user to take action.
     * If the component doesn't need validation or if everything is already valid, nothing will happen.
     */
    fun highlightValidationErrors() {
        componentView?.highlightValidationErrors()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (isInteractionBlocked) return true
        return super.onInterceptTouchEvent(ev)
    }
}
