/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.ui.model

/**
 * The address fields whose order, label and optionality depend on the country. The country itself is not one of them:
 * it is always the first row.
 */
internal enum class AddressField {
    STREET,
    HOUSE_NUMBER_OR_NAME,
    POSTAL_CODE,
    CITY,
    STATE_OR_PROVINCE,
}
