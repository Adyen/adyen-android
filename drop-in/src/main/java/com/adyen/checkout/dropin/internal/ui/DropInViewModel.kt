/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.dropin.internal.DropInResultContract
import kotlin.reflect.KClass

internal class DropInViewModel(
    input: DropInResultContract.Input?,
) : ViewModel() {

    lateinit var backStack: SnapshotStateList<NavKey>

    init {
        initializeBackStack()
    }

    private fun initializeBackStack() {
        // TODO - Check if there are stored payment methods, if not replace preselected with payment method list
        backStack = mutableStateListOf(EmptyNavKey, PreselectedPaymentMethodNavKey)
    }

    class Factory(
        private val inputProvider: () -> DropInResultContract.Input?,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return DropInViewModel(
                input = inputProvider(),
            ) as T
        }
    }
}
