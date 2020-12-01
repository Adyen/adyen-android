/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/12/2020.
 */

package com.adyen.checkout.dropin.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <ViewModelT : ViewModel> viewModelFactory(crossinline f: () -> ViewModelT) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
    }

inline fun <reified ViewModelT : ViewModel> AppCompatActivity.getViewModel(crossinline f: () -> ViewModelT): ViewModelT {
    return ViewModelProvider(this, viewModelFactory(f)).get(ViewModelT::class.java)
}

inline fun <reified ViewModelT : ViewModel> Fragment.getViewModel(crossinline f: () -> ViewModelT): ViewModelT {
    return ViewModelProvider(this, viewModelFactory(f)).get(ViewModelT::class.java)
}

inline fun <reified ViewModelT : ViewModel> Fragment.getActivityViewModel(crossinline f: () -> ViewModelT): ViewModelT {
    return ViewModelProvider(requireActivity(), viewModelFactory(f)).get(ViewModelT::class.java)
}
