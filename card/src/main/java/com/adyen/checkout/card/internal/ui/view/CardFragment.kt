/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/2/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.card.databinding.FragmentCardBinding
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.card.internal.util.CardScannerWrapper
import com.adyen.checkout.core.ui.model.ExpiryDate
import kotlinx.coroutines.launch

internal class CardFragment : Fragment() {

    private var _binding: FragmentCardBinding? = null
    private val binding: FragmentCardBinding get() = requireNotNull(_binding)

    private var delegate: CardDelegate? = null
    private var cardScanner: CardScannerWrapper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scanButton.setOnClickListener {
            cardScanner?.startScanner(this, REQUEST_CODE)
        }
    }

    fun initialize(delegate: CardDelegate) {
        this.delegate = delegate
        this.cardScanner = CardScannerWrapper()
        resetScanner()
    }

    private fun resetScanner() {
        val cardScanner = cardScanner ?: return
        val delegate = delegate ?: return
        cardScanner.terminate()
        viewLifecycleOwner.lifecycleScope.launch {
            val didInitialize = cardScanner.initialize(requireContext(), delegate.componentParams.environment)
            binding.scanButton.isVisible = didInitialize
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE) return

        val result = cardScanner?.getResult(data)

        result?.let {
            delegate?.updateInputData {
                val (pan, expiryMonth, expiryYear) = result
                cardNumber = pan.orEmpty()
                if (expiryMonth != null && expiryYear != null) {
                    expiryDate = ExpiryDate(expiryMonth, expiryYear)
                }
            }
        }
        resetScanner()
    }

    override fun onDestroyView() {
        _binding = null
        delegate = null
        cardScanner?.terminate()
        cardScanner = null
        super.onDestroyView()
    }

    companion object {
        private const val REQUEST_CODE = 101
    }
}
