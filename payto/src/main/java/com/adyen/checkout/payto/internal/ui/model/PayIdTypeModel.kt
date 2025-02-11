/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 11/2/2025.
 */

package com.adyen.checkout.payto.internal.ui.model

import com.adyen.checkout.ui.core.internal.ui.SimpleTextListItem

internal class PayIdTypeModel(
    val type: PayIdType,
    val name: String
) : SimpleTextListItem(name)
