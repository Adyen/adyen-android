/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/5/2024.
 */

package com.adyen.checkout.twint.internal.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ch.twint.payment.sdk.Twint
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.twint.databinding.FragmentTwintBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class TwintFragment : Fragment() {

    private var _binding: FragmentTwintBinding? = null
    private val binding: FragmentTwintBinding get() = requireNotNull(_binding)

    private var twintDelegate: TwintDelegate? = null

    private var twint: Twint? = Twint(this, ::onTwintResult)

    private var twintResultQueue: TwintPayResult? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTwintBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun initialize(delegate: TwintDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        adyenLog(AdyenLogLevel.ERROR) { "initialize" }

        binding.paymentInProgressView.initView(delegate, coroutineScope, localizedContext)

        twintDelegate = delegate
        delegate.payEventFlow
            .onEach { twint?.payWithCode(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        twintResultQueue?.let {
            adyenLog(AdyenLogLevel.ERROR) { "initialize: executing queue" }
            onTwintResult(it)
        }
    }

    private fun onTwintResult(result: TwintPayResult) {
        adyenLog(AdyenLogLevel.ERROR) { "onTwintResult" }
        twintDelegate
            ?.handleTwintResult(result)
            ?.also {
                adyenLog(AdyenLogLevel.ERROR) { "onTwintResult: clearing queue" }
                twintResultQueue = null
            } ?: run {
            adyenLog(AdyenLogLevel.ERROR) { "onTwintResult: setting queue" }
            twintResultQueue = result
        }
    }

    override fun onDestroyView() {
        twint = null
        _binding = null
        super.onDestroyView()
    }
}
