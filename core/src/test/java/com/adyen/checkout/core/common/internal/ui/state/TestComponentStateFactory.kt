/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/7/2025.
 */

package com.adyen.checkout.core.common.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory

internal class TestComponentStateFactory(
    private val defaultComponentState: TestComponentState
) : ComponentStateFactory<TestComponentState, TestFieldId> {
    override fun createDefaultComponentState(): TestComponentState = defaultComponentState
    override fun getFieldIds(): List<TestFieldId> = TestFieldId.entries.toList()
}
