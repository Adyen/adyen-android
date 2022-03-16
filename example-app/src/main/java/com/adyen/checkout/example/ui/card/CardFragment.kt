package com.adyen.checkout.example.ui.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.adyen.checkout.example.databinding.FragmentCardBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal class CardFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCardBinding? = null
    private val binding: FragmentCardBinding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun show(fragmentManager: FragmentManager) = CardFragment().show(fragmentManager, null)
    }
}
