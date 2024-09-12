/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/10/2022.
 */

package com.adyen.checkout.example.di

import com.adyen.checkout.example.ui.theme.DefaultUIThemeRepository
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeModule {

    @Binds
    internal abstract fun bindUIThemeRepository(repository: DefaultUIThemeRepository): UIThemeRepository
}
