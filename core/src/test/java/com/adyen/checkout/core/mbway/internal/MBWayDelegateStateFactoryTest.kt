/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.core.mbway.internal

import com.adyen.checkout.core.mbway.internal.ui.MBWayDelegateStateFactory
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MBWayDelegateStateFactoryTest {

    private lateinit var factory: MBWayDelegateStateFactory

    @BeforeEach
    fun setup() {
        factory = MBWayDelegateStateFactory()
    }

    @Test
    fun `when createDefaultDelegateState is called, then state should contain supported and default countries`() {
        // TODO Test to be added
    }

    @Test
    fun `when getFieldIds is called, then it should return the list of MBWayFieldIds`() {
        val fieldIds = factory.getFieldIds()

        assertEquals(MBWayFieldId.entries, fieldIds)
    }
}
