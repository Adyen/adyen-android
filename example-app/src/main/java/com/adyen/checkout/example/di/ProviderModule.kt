/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.di

import android.app.Application
import com.adyen.checkout.example.provider.LocaleProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {

    @Provides
    internal fun provideLocaleProvider(
        appContext: Application,
    ): LocaleProvider = LocaleProvider(appContext)
}
