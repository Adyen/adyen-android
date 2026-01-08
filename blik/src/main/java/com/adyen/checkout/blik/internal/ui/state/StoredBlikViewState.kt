/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/1/2026.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ViewState

internal data class StoredBlikViewState(
    val isLoading: Boolean,
) : ViewState
