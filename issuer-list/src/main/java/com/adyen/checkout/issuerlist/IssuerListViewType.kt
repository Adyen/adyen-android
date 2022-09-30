/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/9/2022.
 */

package com.adyen.checkout.issuerlist

/**
 * Represents the multiple view types the can be displayed with an issuer list component.
 */
enum class IssuerListViewType {
    /**
     * A simple list of issuers inside a recycler view.
     */
    RECYCLER_VIEW,

    /**
     * A spinner containing all the issuers.
     */
    SPINNER_VIEW
}
