/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

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
