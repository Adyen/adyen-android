/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 24/1/2023.
 */

package com.adyen.checkout.ach

import com.adyen.checkout.components.ui.AddressFieldPolicy
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class AddressFieldPolicyParams : AddressFieldPolicy {

    /**
     * Address form fields will be required.
     */
    @Parcelize
    object Required : AddressFieldPolicyParams()
}
