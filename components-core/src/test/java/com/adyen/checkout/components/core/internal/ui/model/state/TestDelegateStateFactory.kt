/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.state

internal class TestDelegateStateFactory(
    private val defaultDelegateState: TestDelegateState
) : DelegateStateFactory<TestDelegateState, TestFieldId> {
    override fun createDefaultDelegateState(): TestDelegateState = defaultDelegateState
    override fun getFieldIds(): List<TestFieldId> = TestFieldId.entries.toList()
}
