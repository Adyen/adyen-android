/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2023.
 */

package com.adyen.checkout.example.ui.googlepay

import androidx.fragment.app.FragmentManager
import com.adyen.checkout.example.extensions.getLogTag
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GooglePayFragment : BottomSheetDialogFragment() {

    companion object {

        private val TAG = getLogTag()

        fun show(fragmentManager: FragmentManager) {
            GooglePayFragment().show(fragmentManager, TAG)
        }
    }
}
