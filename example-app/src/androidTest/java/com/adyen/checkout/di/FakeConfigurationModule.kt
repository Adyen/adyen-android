/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.di

import com.adyen.checkout.example.di.ConfigurationModule
import com.adyen.checkout.example.ui.configuration.ConfigurationProvider
import com.adyen.checkout.fake.FakeCheckoutConfigurationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ConfigurationModule::class],
)
internal abstract class FakeConfigurationModule {

    @Singleton
    @Binds
    abstract fun bindConfigurationProvider(provider: FakeCheckoutConfigurationProvider): ConfigurationProvider
}
