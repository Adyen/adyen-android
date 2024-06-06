/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2022.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.action.core.GenericActionComponent
import com.adyen.checkout.action.core.internal.provider.GenericActionComponentProvider
import com.adyen.checkout.components.core.ActionComponentCallback
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.PermissionHandlerCallback
import com.adyen.checkout.core.exception.CancellationException
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.FragmentGenericActionComponentBinding
import com.adyen.checkout.dropin.internal.util.arguments
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

@SuppressWarnings("TooManyFunctions")
internal class ActionComponentDialogFragment :
    DropInBottomSheetDialogFragment(),
    ActionComponentCallback {

    private var _binding: FragmentGenericActionComponentBinding? = null
    private val binding: FragmentGenericActionComponentBinding get() = requireNotNull(_binding)

    private val actionComponentViewModel: ActionComponentViewModel by viewModels()

    private val action: Action by arguments(ACTION)
    private val checkoutConfiguration: CheckoutConfiguration by arguments(CHECKOUT_CONFIGURATION)
    private lateinit var actionComponent: GenericActionComponent

    private var permissionCallback: PermissionHandlerCallback? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultsMap ->
            resultsMap.firstNotNullOf { result ->
                val requestedPermission = result.key
                val isGranted = result.value
                if (isGranted) {
                    adyenLog(AdyenLogLevel.DEBUG) { "Permission $requestedPermission granted" }
                    permissionCallback?.onPermissionGranted(requestedPermission)
                } else {
                    adyenLog(AdyenLogLevel.DEBUG) { "Permission $requestedPermission denied" }
                    permissionCallback?.onPermissionDenied(requestedPermission)
                }
                permissionCallback = null
            }
        }

    private val navigationSource: NavigationSource
        get() = when {
            shouldFinishWithAction() -> NavigationSource.ACTION
            else -> NavigationSource.NO_SOURCE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adyenLog(AdyenLogLevel.DEBUG) { "onCreate" }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGenericActionComponentBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = super.onCreateDialog(savedInstanceState).apply {
        window?.setWindowAnimations(UICoreR.style.AdyenCheckout_BottomSheet_NoWindowEnterDialogAnimation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adyenLog(AdyenLogLevel.DEBUG) { "onViewCreated" }
        initObservers()
        initToolbar()

        try {
            val analyticsManager = dropInViewModel.analyticsManager
            val dropInOverrideParams = dropInViewModel.getDropInOverrideParams()
            actionComponent = GenericActionComponentProvider(analyticsManager, dropInOverrideParams).get(
                fragment = this,
                checkoutConfiguration = checkoutConfiguration,
                callback = this,
            )

            actionComponent.setOnRedirectListener { protocol.onRedirect() }

            if (shouldFinishWithAction()) {
                binding.buttonFinish.apply {
                    isVisible = true
                    setOnClickListener { protocol.finishWithAction() }
                }
            }

            binding.componentView.attach(actionComponent, viewLifecycleOwner)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    private fun initToolbar() = with(binding.bottomSheetToolbar) {
        setOnButtonClickListener {
            performBackAction()
        }

        val toolbarMode = when (navigationSource) {
            NavigationSource.ACTION -> DropInBottomSheetToolbarMode.CLOSE_BUTTON
            NavigationSource.NO_SOURCE -> DropInBottomSheetToolbarMode.CLOSE_BUTTON
        }
        setMode(toolbarMode)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        onActionComponentDataChanged(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        adyenLog(AdyenLogLevel.DEBUG) { "onError" }
        handleError(componentError)
    }

    override fun onPermissionRequest(requiredPermission: String, permissionCallback: PermissionHandlerCallback) {
        this.permissionCallback = permissionCallback
        adyenLog(AdyenLogLevel.DEBUG) { "Permission request information dialog shown" }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.checkout_rationale_title_storage_permission)
            .setMessage(R.string.checkout_rationale_message_storage_permission)
            .setOnDismissListener {
                adyenLog(AdyenLogLevel.DEBUG) { "Permission $requiredPermission requested" }
                requestPermissionLauncher.launch(arrayOf(requiredPermission))
            }
            .setPositiveButton(UICoreR.string.error_dialog_button) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun initObservers() {
        actionComponentViewModel.eventsFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                when (it) {
                    ActionComponentFragmentEvent.HANDLE_ACTION -> {
                        handleAction(action)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    fun handleAction(action: Action) {
        actionComponent.handleAction(action, requireActivity())
    }

    override fun onBackPressed() = performBackAction()

    private fun performBackAction(): Boolean {
        // polling will be canceled by lifecycle event
        when (navigationSource) {
            NavigationSource.ACTION -> protocol.finishWithAction()
            NavigationSource.NO_SOURCE -> protocol.terminateDropIn()
        }
        return true
    }

    override fun onCancel(dialog: DialogInterface) {
        adyenLog(AdyenLogLevel.DEBUG) { "onCancel" }
        if (shouldFinishWithAction()) {
            protocol.finishWithAction()
        } else {
            protocol.terminateDropIn()
        }
    }

    private fun onActionComponentDataChanged(actionComponentData: ActionComponentData?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onActionComponentDataChanged" }
        if (actionComponentData != null) {
            protocol.requestDetailsCall(actionComponentData)
        }
    }

    private fun handleError(componentError: ComponentError) {
        when (componentError.exception) {
            is CancellationException -> {
                adyenLog(AdyenLogLevel.DEBUG) { "Flow was cancelled by user" }
                performBackAction()
            }

            else -> {
                adyenLog(AdyenLogLevel.ERROR) { componentError.errorMessage }
                protocol.showError(null, getString(R.string.action_failed), componentError.errorMessage, true)
            }
        }
    }

    private fun shouldFinishWithAction(): Boolean {
        return !GenericActionComponent.PROVIDER.providesDetails(action)
    }

    fun handleIntent(intent: Intent) {
        adyenLog(AdyenLogLevel.DEBUG) { "handleAction" }
        actionComponent.handleIntent(intent)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    internal enum class NavigationSource {
        ACTION,
        NO_SOURCE,
    }

    companion object {
        private const val ACTION = "ACTION"
        private const val CHECKOUT_CONFIGURATION = "CHECKOUT_CONFIGURATION"

        fun newInstance(
            action: Action,
            checkoutConfiguration: CheckoutConfiguration,
        ): ActionComponentDialogFragment {
            return ActionComponentDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ACTION, action)
                    putParcelable(CHECKOUT_CONFIGURATION, checkoutConfiguration)
                }
            }
        }
    }
}
