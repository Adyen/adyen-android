/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/5/2024.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adyen.checkout.googlepay.databinding.FragmentGooglePayBinding
import com.google.android.gms.wallet.contract.TaskResultContracts

internal class GooglePayFragment : Fragment() {

    private var _binding: FragmentGooglePayBinding? = null
    private val binding: FragmentGooglePayBinding get() = requireNotNull(_binding)

    private var delegate: GooglePayDelegate? = null

    private val googlePayLauncher = registerForActivityResult(TaskResultContracts.GetPaymentDataResult()) {
        delegate?.handlePaymentResult(it)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGooglePayBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun initialize(delegate: GooglePayDelegate) {
        this.delegate = delegate
        delegate.setPaymentDataLauncher(googlePayLauncher)
    }

    override fun onDestroyView() {
        delegate = null
        _binding = null
        super.onDestroyView()
    }
}
