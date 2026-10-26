/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/10/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle

/**
 * Returns a new [SavedStateHandle] with the same values, like the one a view model gets after process death.
 */
internal fun SavedStateHandle.rebuildFromSavedState(): SavedStateHandle =
    SavedStateHandle(keys().associateWith { key -> get<Any?>(key) })
