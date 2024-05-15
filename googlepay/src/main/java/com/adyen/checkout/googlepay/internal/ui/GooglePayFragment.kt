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

internal class GooglePayFragment : Fragment() {

    private var _binding: FragmentGooglePayBinding? = null
    private val binding: FragmentGooglePayBinding get() = requireNotNull(_binding)

    private var _delegate: GooglePayDelegate? = null
    private val delegate: GooglePayDelegate get() = requireNotNull(_delegate)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGooglePayBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun initialize(delegate: GooglePayDelegate) {
        _delegate = delegate
    }

    override fun onDestroyView() {
        _delegate = null
        _binding = null
        super.onDestroyView()
    }
}
