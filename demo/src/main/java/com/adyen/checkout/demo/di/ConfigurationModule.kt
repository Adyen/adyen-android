/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/2/2024.
 */

package com.adyen.checkout.demo.di

import android.app.Application
import com.adyen.checkout.demo.ui.configuration.MyStoreDemoConfigurationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ConfigurationModule {

    @Provides
    fun provideConfigurationProvider(appContext: Application) = MyStoreDemoConfigurationProvider(appContext)
}
