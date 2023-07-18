/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/5/2023.
 */

package com.adyen.checkout.example.ui.main

/*
This is a data class instead of a sealed class because the list will always be displayed. The loading indicator only
shows up on top of the list but does not hide it from the view.
 */
internal data class MainViewState(
    val listItems: List<ComponentItem>,
    val useSessions: Boolean,
    val showLoading: Boolean,
)
