/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */
package com.adyen.checkout.core.internal.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class JsonUtilsTest {

    @Test
    fun `when calling getStringOrNull with a non null string then result should be that string`() {
        val jsonObject = JSONObject(
            """
            { "key": "value" }
            """.trimIndent(),
        )
        val result = jsonObject.getStringOrNull("key")
        assertEquals("value", result)
    }

    @Test
    fun `when calling getStringOrNull with a null string then result should be null`() {
        val jsonObject = JSONObject(
            """
            { "key": null }
            """.trimIndent(),
        )
        val result = jsonObject.getStringOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getStringOrNull with a non existent string then result should be null`() {
        val jsonObject = JSONObject()
        val result = jsonObject.getStringOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getIntOrNull with a non null integer then result should be that integer`() {
        val jsonObject = JSONObject(
            """
            { "key": 1 }
            """.trimIndent(),
        )
        val result = jsonObject.getIntOrNull("key")
        assertEquals(1, result)
    }

    @Test
    fun `when calling getIntOrNull with a null integer then result should be null`() {
        val jsonObject = JSONObject(
            """
            { "key": null }
            """.trimIndent(),
        )
        val result = jsonObject.getIntOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getIntOrNull with a non existent integer then result should be null`() {
        val jsonObject = JSONObject()
        val result = jsonObject.getIntOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getBooleanOrNull with a non null boolean then result should be that boolean`() {
        val jsonObject = JSONObject(
            """
            { "key": true }
            """.trimIndent(),
        )
        val result = jsonObject.getBooleanOrNull("key")
        assertEquals(true, result)
    }

    @Test
    fun `when calling getBooleanOrNull with a null boolean then result should be null`() {
        val jsonObject = JSONObject(
            """
            { "key": null }
            """.trimIndent(),
        )
        val result = jsonObject.getBooleanOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getBooleanOrNull with a non existent boolean then result should be null`() {
        val jsonObject = JSONObject()
        val result = jsonObject.getBooleanOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getLongOrNull with a non null long then result should be that long`() {
        val jsonObject = JSONObject(
            """
            { "key": 92233720368547758 }
            """.trimIndent(),
        )
        val result = jsonObject.getLongOrNull("key")
        assertEquals(92233720368547758L, result)
    }

    @Test
    fun `when calling getLongOrNull with a null long then result should be null`() {
        val jsonObject = JSONObject(
            """
            { "key": null }
            """.trimIndent(),
        )
        val result = jsonObject.getLongOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getLongOrNull with a non existent long then result should be null`() {
        val jsonObject = JSONObject()
        val result = jsonObject.getLongOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getDoubleOrNull with a non null double then result should be that double`() {
        val jsonObject = JSONObject(
            """
            { "key": 13.37 }
            """.trimIndent(),
        )
        val result = jsonObject.getDoubleOrNull("key")
        assertEquals(13.37, result)
    }

    @Test
    fun `when calling getDoubleOrNull with a null double then result should be null`() {
        val jsonObject = JSONObject(
            """
            { "key": null }
            """.trimIndent(),
        )
        val result = jsonObject.getDoubleOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling getDoubleOrNull with a non existent double then result should be null`() {
        val jsonObject = JSONObject()
        val result = jsonObject.getDoubleOrNull("key")
        assertNull(result)
    }

    @Test
    fun `when calling parseOptStringList with a non empty JSON array then result should be matching string list`() {
        val jsonObject = JSONObject(
            """
            {
                "array":["value1", "value2", "value3"]
            }
            """.trimIndent(),
        )
        val result = JsonUtils.parseOptStringList(jsonObject.optJSONArray("array"))
        assertEquals(listOf("value1", "value2", "value3"), result)
    }

    @Test
    fun `when calling parseOptStringList with an empty JSON array then result should be a empty list`() {
        val jsonObject = JSONObject(
            """
            {
                "array":[]
            }
            """.trimIndent(),
        )
        val result = JsonUtils.parseOptStringList(jsonObject.optJSONArray("array"))
        assertTrue(result != null && result.isEmpty())
    }

    @Test
    fun `when calling parseOptStringList with a null JSON array then result should be null`() {
        val jsonObject = JSONObject(
            """
            {
                "array": null
            }
            """.trimIndent(),
        )
        val result = JsonUtils.parseOptStringList(jsonObject.optJSONArray("array"))
        assertNull(result)
    }

    @Test
    fun `when calling parseOptStringList with a non existent JSON array then result should be null`() {
        val jsonObject = JSONObject()
        val result = JsonUtils.parseOptStringList(jsonObject.optJSONArray("array"))
        assertNull(result)
    }
}
