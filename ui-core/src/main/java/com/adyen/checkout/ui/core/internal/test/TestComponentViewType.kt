/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/2/2023.
 */

package com.adyen.checkout.ui.core.internal.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

/**
 * Test implementation of [ComponentViewType]. This class should never be used except in test code.
 */
// TODO move to test fixtures once it becomes supported on Android
@RestrictTo(RestrictTo.Scope.TESTS)
enum class TestComponentViewType : ComponentViewType {
    VIEW_TYPE_1,
    VIEW_TYPE_2,
    VIEW_TYPE_3;

    override val viewProvider: ViewProvider
        get() = throw IllegalStateException("Method should not be called in tests.")
}
