/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.ui.core.internal.ui.model.AddressFieldPolicy

internal sealed class AddressFieldPolicyParams : AddressFieldPolicy {

    /**
     * Address form fields will be required.
     */
    object Required : AddressFieldPolicyParams()
}
