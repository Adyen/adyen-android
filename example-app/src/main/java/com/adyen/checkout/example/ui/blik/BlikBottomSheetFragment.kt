/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 21/11/2022.
 */

package com.adyen.checkout.example.ui.blik

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.await.AwaitComponent
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.example.databinding.FragmentBlikActionBottomSheetBinding
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BlikBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentBlikActionBottomSheetBinding? = null
    private val binding: FragmentBlikActionBottomSheetBinding get() = requireNotNull(_binding)
    private var awaitComponent: AwaitComponent? = null

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private val action: Action by lazy { arguments?.get(ACTION) as Action }
    private val awaitConfiguration: AwaitConfiguration by lazy {
        arguments?.get(ACTION_CONFIGURATION) as AwaitConfiguration
    }
    lateinit var blikListener: BlikListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is BlikListener) {
            blikListener = activity as BlikListener
        } else {
            throw IllegalArgumentException("Host activity needs to implement BlikListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBlikActionBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        awaitComponent = AwaitComponent.PROVIDER.get(
            this,
            requireActivity().application,
            awaitConfiguration
        ).apply {
            observe(this@BlikBottomSheetFragment, blikListener::onActionComponentEvent)
            handleAction(action, requireActivity())
        }
    }

    companion object {
        const val ACTION = "ACTION"
        const val ACTION_CONFIGURATION = "ACTION_CONFIGURATION"

        fun newInstance(action: Action, awaitConfiguration: AwaitConfiguration): BlikBottomSheetFragment {
            val args = Bundle()
            args.putParcelable(ACTION, action)
            args.putParcelable(ACTION_CONFIGURATION, awaitConfiguration)

            val blikBottomSheetFragment = BlikBottomSheetFragment()
            blikBottomSheetFragment.arguments = args
            return blikBottomSheetFragment
        }
    }
}

interface BlikListener {
    fun onActionComponentEvent(event: ActionComponentEvent)
}
