/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.internal.ButtonComponent
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.databinding.FragmentGenericComponentBinding
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent

internal class GenericComponentDialogFragment : BaseComponentDialogFragment() {

    private var _binding: FragmentGenericComponentBinding? = null
    private val binding: FragmentGenericComponentBinding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGenericComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adyenLog(AdyenLogLevel.DEBUG) { "onViewCreated" }
        binding.header.text = paymentMethod.name

        try {
            attachComponent(component)
        } catch (e: CheckoutException) {
            handleError(ComponentError(e))
        }
    }

    private fun attachComponent(component: PaymentComponent) {
        if (component is ViewableComponent) {
            binding.componentView.attach(component, viewLifecycleOwner)

            if ((component as? ButtonComponent)?.isConfirmationRequired() == true) {
                binding.componentView.requestFocus()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object : BaseCompanion<GenericComponentDialogFragment>(GenericComponentDialogFragment::class.java)
}
