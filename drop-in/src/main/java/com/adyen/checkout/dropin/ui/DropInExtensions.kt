/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/12/2020.
 */

package com.adyen.checkout.dropin.ui

import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@MainThread
inline fun <ViewModelT : ViewModel> viewModelFactory(crossinline factoryProducer: () -> ViewModelT) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = factoryProducer() as T
    }

@MainThread
inline fun <reified ViewModelT : ViewModel> AppCompatActivity.getViewModel(crossinline factoryProducer: () -> ViewModelT): ViewModelT {
    return ViewModelProvider(this, viewModelFactory(factoryProducer)).get(ViewModelT::class.java)
}

@MainThread
inline fun <reified ViewModelT : ViewModel> Fragment.getViewModel(
    crossinline f: () -> ViewModelT
): ViewModelT {
    return ViewModelProvider(this, viewModelFactory(f)).get(ViewModelT::class.java)
}

@MainThread
inline fun <reified ViewModelT : ViewModel> Fragment.getActivityViewModel(crossinline f: () -> ViewModelT): ViewModelT {
    return ViewModelProvider(requireActivity(), viewModelFactory(f)).get(ViewModelT::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> AppCompatActivity.viewModelsFactory(
    crossinline factoryProducer: () -> VM
): Lazy<VM> {
    return viewModels { viewModelFactory(factoryProducer) }
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModelsFactory(
    crossinline factoryProducer: () -> VM
): Lazy<VM> {
    return viewModels { viewModelFactory(factoryProducer) }
}
