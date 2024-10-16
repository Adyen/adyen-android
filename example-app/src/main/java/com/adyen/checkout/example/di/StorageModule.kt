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
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class StorageModule {

    @Binds
    abstract fun bindKeyValueStorage(defaultKeyValueStorage: DefaultKeyValueStorage): KeyValueStorage

    companion object {

        @Provides
        fun provideSharedPreferences(appContext: Application): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(appContext)

        @Provides
        fun provideAssetManager(appContext: Application): AssetManager = appContext.assets
    }
}
