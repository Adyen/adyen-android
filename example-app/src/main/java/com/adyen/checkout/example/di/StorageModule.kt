/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/10/2019.
 */

package com.adyen.checkout.example.di

import androidx.preference.PreferenceManager
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.data.storage.KeyValueStorageImpl
import org.koin.dsl.module

val storageManager = module {
    single { PreferenceManager.getDefaultSharedPreferences(get()) }
    single<KeyValueStorage> { KeyValueStorageImpl(get(), get()) }
}
