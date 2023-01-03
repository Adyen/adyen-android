/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/1/2023.
 */

package com.adyen.checkout.example.ui.bacs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.adyen.checkout.example.databinding.FragmentBacsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BacsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBacsBinding? = null
    private val binding: FragmentBacsBinding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBacsBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val TAG = "BacsFragment"

        fun show(fragmentManager: FragmentManager) {
            BacsFragment().show(fragmentManager, TAG)
        }
    }
}
