/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/10/2019.
 */

package com.adyen.checkout.example.di

import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.data.api.RecurringApiService
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.repositories.PaymentsRepositoryImpl
import com.adyen.checkout.example.repositories.RecurringRepository
import com.adyen.checkout.example.repositories.RecurringRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    internal fun providePaymentsRepository(
        checkoutApiService: CheckoutApiService
    ): PaymentsRepository = PaymentsRepositoryImpl(checkoutApiService)

    @Provides
    internal fun provideRecurringRepository(
        recurringApiService: RecurringApiService
    ): RecurringRepository = RecurringRepositoryImpl(recurringApiService)
}
