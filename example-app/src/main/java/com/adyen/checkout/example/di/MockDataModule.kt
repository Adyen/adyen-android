/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/1/2024.
 */

package com.adyen.checkout.example.di

import android.app.Application
import com.adyen.checkout.example.data.mock.MockDataService
import com.adyen.checkout.example.data.mock.MockDataServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MockDataModule {

    @Provides
    fun provideMockDataRepository(appContext: Application): MockDataService =
        MockDataServiceImpl(appContext.assets)
}
