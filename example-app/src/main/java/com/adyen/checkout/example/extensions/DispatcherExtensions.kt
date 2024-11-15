/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/11/2024.
 */

package com.adyen.checkout.example.extensions

import com.adyen.checkout.core.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

@Suppress("RestrictedApi")
val IODispatcher: CoroutineDispatcher get() = DispatcherProvider.IO
