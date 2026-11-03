/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.state.PickerViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.ui.internal.element.ValuePickerItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AddressRegionPickerTest {

    @Test
    fun `when the regions are mapped then each item shows the name as title and the code as subtitle`() {
        // GIVEN
        val viewState = pickerViewState(selected = null)

        // WHEN
        val items = viewState.toValuePickerItems()

        // THEN
        val expected = listOf(
            ValuePickerItem(id = "CA", title = "California", subtitle = "CA", isSelected = false),
            ValuePickerItem(id = "NY", title = "New York", subtitle = "NY", isSelected = false),
        )
        assertEquals(expected, items)
    }

    @Test
    fun `when a region is selected then only its item is selected`() {
        // GIVEN
        val viewState = pickerViewState(selected = NEW_YORK)

        // WHEN
        val items = viewState.toValuePickerItems()

        // THEN
        assertEquals(listOf(false, true), items.map { it.isSelected })
    }

    @Test
    fun `when the selection is not in the list then no item is selected`() {
        // GIVEN
        // A saved state is selected before its states load, and the row then names it by its code.
        val viewState = pickerViewState(selected = AddressRegion(code = "TX", name = "TX"))

        // WHEN
        val items = viewState.toValuePickerItems()

        // THEN
        assertEquals(listOf(false, false), items.map { it.isSelected })
    }

    @Test
    fun `when the selection has the code of a region but not its name then the region is selected`() {
        // GIVEN
        val viewState = pickerViewState(selected = AddressRegion(code = "CA", name = "CA"))

        // WHEN
        val items = viewState.toValuePickerItems()

        // THEN
        assertEquals(listOf(true, false), items.map { it.isSelected })
    }

    private fun pickerViewState(selected: AddressRegion?) = PickerViewState(
        title = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
        regions = listOf(CALIFORNIA, NEW_YORK),
        selected = selected,
    )

    companion object {
        private val CALIFORNIA = AddressRegion(code = "CA", name = "California")
        private val NEW_YORK = AddressRegion(code = "NY", name = "New York")
    }
}
