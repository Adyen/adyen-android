/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/4/2022.
 */

package com.adyen.checkout.core.di

import android.app.Application

class CheckoutServiceLocator(val application: Application) {


    companion object {
        private lateinit var INSTANCE: CheckoutServiceLocator

        fun getInstance(application: Application): CheckoutServiceLocator {
            return if (::INSTANCE.isInitialized) {
                INSTANCE
            } else {
                CheckoutServiceLocator(application)
            }
        }
    }
}