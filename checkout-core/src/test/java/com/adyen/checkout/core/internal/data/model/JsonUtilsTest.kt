/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */
package com.adyen.checkout.core.internal.data.model

import org.json.JSONArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class JsonUtilsTest {

    @Test
    fun parseOptStringList_Pass_ParseStringArray() {
        val jsonArray = JSONArray()
        val testString = "Test"
        jsonArray.put(testString)
        val stringList = JsonUtils.parseOptStringList(jsonArray)
        assertNotNull(stringList)
        val first = stringList!![0]
        assertEquals(testString, first)
    }

    @Test
    fun parseOptStringList_Pass_ParseEmptyArray() {
        val jsonArray = JSONArray()
        val stringList = JsonUtils.parseOptStringList(jsonArray)
        assertNotNull(stringList)
        assertTrue(stringList!!.isEmpty())
    }

    @Test
    fun parseOptStringList_Pass_ParseNull() {
        val stringList = JsonUtils.parseOptStringList(null)
        assertNull(stringList)
    }
}
