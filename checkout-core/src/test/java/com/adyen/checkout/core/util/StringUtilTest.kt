/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 16/3/2022.
 */

package com.adyen.checkout.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class StringUtilTest {

    @Test
    fun `when value has spaces and specified character - remove all spaces and character`() {
        val initial = "1234  0056"
        val expected = "123456"
        val result = StringUtil.normalize(initial, '0')
        println("Result: $result")
        assertEquals(expected, result)
    }
}
