/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2022.
 */

package com.adyen.checkout.components.ui

import com.adyen.checkout.components.ui.view.ComponentViewType
import kotlinx.coroutines.flow.Flow

interface ViewProvidingComponent {
    val viewFlow: Flow<ComponentViewType?>
}
