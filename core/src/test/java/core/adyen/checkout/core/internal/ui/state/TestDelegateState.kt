/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/3/2025.
 */

package core.adyen.checkout.core.internal.ui.state

import com.adyen.checkout.core.internal.ui.state.DelegateState

internal class TestDelegateState(
    override val isValid: Boolean = true
) : DelegateState
