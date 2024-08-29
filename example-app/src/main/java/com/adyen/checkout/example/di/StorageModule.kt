/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/10/2019.
 */

package com.adyen.checkout.example.di

import android.app.Application
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.preference.PreferenceManager
import com.adyen.checkout.example.data.storage.DefaultKeyValueStorage
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.data.storage.SharedPreferencesManager
import com.adyen.checkout.example.ui.settings.SettingsEditor
import com.adyen.checkout.example.ui.theme.DefaultUIThemeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    fun provideSharedPreferences(appContext: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(appContext)

    @Provides
    fun provideSharedPreferencesManager(sharedPreferences: SharedPreferences): SharedPreferencesManager =
        SharedPreferencesManager(sharedPreferences)

    @Provides
    fun provideKeyValueStorage(sharedPreferencesManager: SharedPreferencesManager): KeyValueStorage =
        DefaultKeyValueStorage(sharedPreferencesManager)

    @Provides
    fun provideAssetManager(appContext: Application): AssetManager = appContext.assets

    @Provides
    internal fun provideSettingsEditor(
        keyValueStorage: KeyValueStorage,
        uiThemeRepository: DefaultUIThemeRepository,
    ): SettingsEditor = SettingsEditor(keyValueStorage, uiThemeRepository)
}
