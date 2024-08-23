/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2024.
 */

package com.adyen.checkout.example.di

import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.ui.settings.SettingsUIMapper
import com.adyen.checkout.example.ui.theme.DefaultNightThemeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    internal fun provideSettingsUIMapper(
        keyValueStorage: KeyValueStorage,
        nightThemeRepository: DefaultNightThemeRepository,
    ): SettingsUIMapper = SettingsUIMapper(keyValueStorage, nightThemeRepository)
}
