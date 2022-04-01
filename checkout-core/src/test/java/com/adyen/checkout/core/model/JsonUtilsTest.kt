/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */
package com.adyen.checkout.core.model

import com.adyen.checkout.core.model.JsonUtils.parseOptStringList
import org.json.JSONArray
import org.junit.Assert
import org.junit.Test

class JsonUtilsTest {
    @Test
    fun parseOptStringList_Pass_ParseStringArray() {
        val jsonArray = JSONArray()
        val testString = "Test"
        jsonArray.put(testString)
        val stringList = parseOptStringList(jsonArray)
        Assert.assertNotNull(stringList)
        val first = stringList!![0]
        Assert.assertEquals(testString, first)
    }

    @Test
    fun parseOptStringList_Pass_ParseEmptyArray() {
        val jsonArray = JSONArray()
        val stringList = parseOptStringList(jsonArray)
        Assert.assertNotNull(stringList)
        Assert.assertTrue(stringList!!.isEmpty())
    }

    @Test
    fun parseOptStringList_Pass_ParseNull() {
        val stringList = parseOptStringList(null)
        Assert.assertNull(stringList)
    }
}
