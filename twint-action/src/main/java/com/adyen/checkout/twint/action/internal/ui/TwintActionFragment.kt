/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/5/2024.
 */

package com.adyen.checkout.twint.action.internal.ui

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
import com.adyen.checkout.twint.action.databinding.FragmentTwintActionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class TwintActionFragment : Fragment() {

    private var _binding: FragmentTwintActionBinding? = null
    private val binding: FragmentTwintActionBinding get() = requireNotNull(_binding)

    private var twintActionDelegate: TwintActionDelegate? = null

    private var twint: Twint? = Twint(this, ::onTwintResult)

    private var queuedTwintResult: TwintPayResult? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTwintActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun initialize(delegate: TwintActionDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        adyenLog(AdyenLogLevel.DEBUG) { "initialize" }

        binding.paymentInProgressView.initView(delegate, coroutineScope, localizedContext)

        twintActionDelegate = delegate
        delegate.payEventFlow
            .onEach(::onPayEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        queuedTwintResult?.let {
            adyenLog(AdyenLogLevel.DEBUG) { "initialize: executing queue" }
            onTwintResult(it)
        }
    }

    private fun onPayEvent(event: TwintFlowType) {
        when (event) {
            is TwintFlowType.Recurring -> twint?.registerForUOF(event.token)
            is TwintFlowType.Regular -> twint?.payWithCode(event.token)
        }
    }

    private fun onTwintResult(result: TwintPayResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "onTwintResult" }
        twintActionDelegate
            ?.handleTwintResult(result)
            ?.also {
                adyenLog(AdyenLogLevel.DEBUG) { "onTwintResult: clearing queue" }
                queuedTwintResult = null
            } ?: run {
            adyenLog(AdyenLogLevel.DEBUG) { "onTwintResult: setting queue" }
            queuedTwintResult = result
        }
    }

    override fun onDestroyView() {
        twint = null
        _binding = null
        super.onDestroyView()
    }
}
