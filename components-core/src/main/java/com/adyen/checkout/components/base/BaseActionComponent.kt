/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */
package com.adyen.checkout.components.base

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.base.lifecycle.ActionComponentViewModel

@Suppress("TooManyFunctions")
abstract class BaseActionComponent<ConfigurationT : Configuration>(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: ConfigurationT
) : ActionComponentViewModel<ConfigurationT>(savedStateHandle, application, configuration)
