/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/3/2022.
 */

package com.adyen.checkout.card.ui.model

import com.adyen.checkout.components.ui.adapter.SimpleTextListItem

data class AddressListItem(
    val name: String,
    val code: String,
    val selected: Boolean
) : SimpleTextListItem(name)
