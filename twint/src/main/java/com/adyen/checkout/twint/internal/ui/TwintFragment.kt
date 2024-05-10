/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/5/2024.
 */

package com.adyen.checkout.twint.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ch.twint.payment.sdk.Twint
import com.adyen.checkout.twint.databinding.FragmentTwintBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class TwintFragment : Fragment() {

    private var _binding: FragmentTwintBinding? = null
    private val binding: FragmentTwintBinding get() = requireNotNull(_binding)

    private var _twintDelegate: TwintDelegate? = null
    private val twintDelegate: TwintDelegate get() = requireNotNull(_twintDelegate)

    private var twint: Twint? = Twint(this) { twintDelegate.handleTwintResult(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTwintBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun initialize(delegate: TwintDelegate) {
        _twintDelegate = delegate
        twintDelegate.payEventFlow
            .onEach { twint?.payWithCode(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        twint = null
        _twintDelegate = null
        _binding = null
        super.onDestroyView()
    }
}
