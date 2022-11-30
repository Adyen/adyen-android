/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2022.
 */

package com.adyen.checkout.dropin.ui.action

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.action.GenericActionComponent
import com.adyen.checkout.action.GenericActionComponentProvider
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.CancellationException
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentGenericActionComponentBinding
import com.adyen.checkout.dropin.mapToParams
import com.adyen.checkout.dropin.ui.arguments
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.ui.viewmodel.ActionComponentFragmentEvent
import com.adyen.checkout.dropin.ui.viewmodel.ActionComponentViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressWarnings("TooManyFunctions")
internal class ActionComponentDialogFragment : DropInBottomSheetDialogFragment() {

    private var _binding: FragmentGenericActionComponentBinding? = null
    private val binding: FragmentGenericActionComponentBinding get() = requireNotNull(_binding)

    private val actionComponentViewModel: ActionComponentViewModel by viewModels()

    private val action: Action by arguments(ACTION)
    private val actionConfiguration: GenericActionConfiguration by arguments(ACTION_CONFIGURATION)
    private lateinit var actionComponent: GenericActionComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGenericActionComponentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")
        initObservers()
        binding.header.isVisible = false

        try {
            val componentParams = dropInViewModel.dropInConfiguration.mapToParams(dropInViewModel.amount)
            actionComponent = GenericActionComponentProvider(componentParams).get(
                this,
                requireActivity().application,
                actionConfiguration
            )

            if (shouldFinishWithAction()) {
                binding.buttonFinish.apply {
                    isVisible = true
                    setOnClickListener { protocol.finishWithAction() }
                }
            }

            actionComponent.observe(viewLifecycleOwner, ::onActionComponentEvent)

            binding.componentView.attach(actionComponent, viewLifecycleOwner)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    private fun onActionComponentEvent(event: ActionComponentEvent) {
        when (event) {
            is ActionComponentEvent.ActionDetails -> onActionComponentDataChanged(event.data)
            is ActionComponentEvent.Error -> onError(event.error)
        }
    }

    private fun initObservers() {
        actionComponentViewModel.eventsFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                when (it) {
                    ActionComponentFragmentEvent.HANDLE_ACTION -> {
                        actionComponent.handleAction(action, requireActivity())
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onBackPressed(): Boolean {
        // polling will be canceled by lifecycle event
        when {
            shouldFinishWithAction() -> {
                protocol.finishWithAction()
            }
            dropInViewModel.shouldSkipToSinglePaymentMethod() -> {
                protocol.terminateDropIn()
            }
            else -> {
                protocol.showPaymentMethodsDialog()
            }
        }
        return true
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.d(TAG, "onCancel")
        if (shouldFinishWithAction()) {
            protocol.finishWithAction()
        } else {
            protocol.terminateDropIn()
        }
    }

    private fun onActionComponentDataChanged(actionComponentData: ActionComponentData?) {
        Logger.d(TAG, "onActionComponentDataChanged")
        if (actionComponentData != null) {
            protocol.requestDetailsCall(actionComponentData)
        }
    }

    private fun onError(error: ComponentError?) {
        Logger.d(TAG, "onError")
        if (error != null) {
            handleError(error)
        }
    }

    private fun handleError(componentError: ComponentError) {
        when (componentError.exception) {
            is CancellationException -> {
                Logger.d(TAG, "Flow was cancelled by user")
                onBackPressed()
            }
            else -> {
                Logger.e(TAG, componentError.errorMessage)
                protocol.showError(getString(R.string.action_failed), componentError.errorMessage, true)
            }
        }
    }

    private fun shouldFinishWithAction(): Boolean {
        return !GenericActionComponent.PROVIDER.providesDetails(action)
    }

    fun handleIntent(intent: Intent) {
        Logger.d(TAG, "handleAction")
        actionComponent.handleIntent(intent)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        const val ACTION = "ACTION"
        const val ACTION_CONFIGURATION = "ACTION_CONFIGURATION"

        fun newInstance(
            action: Action,
            actionConfiguration: GenericActionConfiguration
        ): ActionComponentDialogFragment {
            val args = Bundle()
            args.putParcelable(ACTION, action)
            args.putParcelable(ACTION_CONFIGURATION, actionConfiguration)

            val componentDialogFragment = ActionComponentDialogFragment()
            componentDialogFragment.arguments = args

            return componentDialogFragment
        }
    }
}
