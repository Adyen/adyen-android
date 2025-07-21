/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.ui.core.old.internal.ui.model.AddressFieldPolicy

internal sealed class AddressFieldPolicyParams : AddressFieldPolicy {

    /**
     * Address form fields will be required.
     */
    object Required : AddressFieldPolicyParams()
}
