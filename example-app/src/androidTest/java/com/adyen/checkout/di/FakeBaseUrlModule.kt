/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.di

import com.adyen.checkout.example.di.BaseUrl
import com.adyen.checkout.example.di.NetworkModule
import com.adyen.checkout.server.CheckoutMockWebServer
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule.BaseUrlModule::class],
)
internal object FakeBaseUrlModule {

    @BaseUrl
    @Provides
    fun provideBaseUrl(): String = CheckoutMockWebServer.baseUrl
}
