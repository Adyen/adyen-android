/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.di

import com.adyen.checkout.demo.data.api.CheckoutApiService
import com.adyen.checkout.demo.repositories.SessionsRepository
import com.adyen.checkout.demo.repositories.SessionsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun providePaymentsRepository(
        checkoutApiService: CheckoutApiService
    ): SessionsRepository = SessionsRepositoryImpl(checkoutApiService)
}

