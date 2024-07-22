/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/7/2024.
 */

package com.adyen.checkout.ui.core.internal.ui

/**
 * Test implementation of [ComponentViewType].
 */
enum class TestComponentViewType : ComponentViewType {
    VIEW_TYPE_1,
    VIEW_TYPE_2,
    VIEW_TYPE_3;

    override val viewProvider: ViewProvider
        get() = error("Method should not be called in tests.")
}
