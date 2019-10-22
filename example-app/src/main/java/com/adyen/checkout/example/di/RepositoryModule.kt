/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/10/2019.
 */

package com.adyen.checkout.example.di

import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    factory<PaymentsRepository> { PaymentsRepositoryImpl(get()) }
}
