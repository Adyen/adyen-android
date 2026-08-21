/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/8/2026.
 */

package com.adyen.checkout.core.common.internal.model

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class JsonUtilsTest {

    @Nested
    inner class GetStringOrNullTest {

        @Test
        fun `when the value is a string, then that string is returned`() {
            val jsonObject = JSONObject("""{"key":"value"}""")

            assertEquals("value", jsonObject.getStringOrNull("key"))
        }

        @Test
        fun `when the value is null, then null is returned`() {
            val jsonObject = JSONObject("""{"key":null}""")

            assertNull(jsonObject.getStringOrNull("key"))
        }

        @Test
        fun `when the key does not exist, then null is returned`() {
            assertNull(JSONObject().getStringOrNull("key"))
        }
    }

    @Nested
    inner class GetBooleanOrNullTest {

        @Test
        fun `when the value is a boolean, then that boolean is returned`() {
            val jsonObject = JSONObject("""{"key":true}""")

            assertEquals(true, jsonObject.getBooleanOrNull("key"))
        }

        @Test
        fun `when the value is null, then null is returned`() {
            val jsonObject = JSONObject("""{"key":null}""")

            assertNull(jsonObject.getBooleanOrNull("key"))
        }

        @Test
        fun `when the key does not exist, then null is returned`() {
            assertNull(JSONObject().getBooleanOrNull("key"))
        }
    }

    @Nested
    inner class GetIntOrNullTest {

        @Test
        fun `when the value is an int, then that int is returned`() {
            val jsonObject = JSONObject("""{"key":1}""")

            assertEquals(1, jsonObject.getIntOrNull("key"))
        }

        @Test
        fun `when the value is null, then null is returned`() {
            val jsonObject = JSONObject("""{"key":null}""")

            assertNull(jsonObject.getIntOrNull("key"))
        }

        @Test
        fun `when the key does not exist, then null is returned`() {
            assertNull(JSONObject().getIntOrNull("key"))
        }

        @Test
        fun `when the value is a numeric string, then it is coerced to an int`() {
            val jsonObject = JSONObject("""{"key":"1"}""")

            assertEquals(1, jsonObject.getIntOrNull("key"))
        }
    }

    @Nested
    inner class GetLongOrNullTest {

        @Test
        fun `when the value is a long, then that long is returned`() {
            val jsonObject = JSONObject("""{"key":92233720368547758}""")

            assertEquals(92233720368547758L, jsonObject.getLongOrNull("key"))
        }

        @Test
        fun `when the value is null, then null is returned`() {
            val jsonObject = JSONObject("""{"key":null}""")

            assertNull(jsonObject.getLongOrNull("key"))
        }

        @Test
        fun `when the key does not exist, then null is returned`() {
            assertNull(JSONObject().getLongOrNull("key"))
        }
    }

    @Nested
    inner class StringListTest {

        @Test
        fun `when parsing a string list, then all entries are returned`() {
            val jsonArray = JSONArray("""["a","b","c"]""")

            assertEquals(listOf("a", "b", "c"), JsonUtils.parseStringList(jsonArray))
        }

        @Test
        fun `when getting a string list by key, then all entries are returned`() {
            val jsonObject = JSONObject("""{"array":["a","b"]}""")

            assertEquals(listOf("a", "b"), jsonObject.getStringList("array"))
        }

        // Unlike optStringList, getStringList is not lenient about a missing key.
        @Test
        fun `when the string list key does not exist, then a JSONException is thrown`() {
            assertThrows(JSONException::class.java) {
                JSONObject().getStringList("array")
            }
        }

        @Test
        fun `when the optional string list holds a null entry, then that entry is skipped`() {
            val jsonArray = JSONArray("""["a",null]""")

            assertEquals(listOf("a"), JsonUtils.parseOptStringList(jsonArray))
        }

        @Test
        fun `when parsing an optional string list, then all entries are returned`() {
            val jsonObject = JSONObject("""{"array":["a","b","c"]}""")

            assertEquals(listOf("a", "b", "c"), jsonObject.optStringList("array"))
        }

        @Test
        fun `when parsing an empty optional string list, then an empty list is returned`() {
            val jsonObject = JSONObject("""{"array":[]}""")

            assertEquals(emptyList<String>(), jsonObject.optStringList("array"))
        }

        @Test
        fun `when parsing a null optional string list, then null is returned`() {
            val jsonObject = JSONObject("""{"array":null}""")

            assertNull(jsonObject.optStringList("array"))
        }

        @Test
        fun `when the optional string list key does not exist, then null is returned`() {
            assertNull(JSONObject().optStringList("array"))
        }

        @Test
        fun `when serializing a string list, then all entries are kept`() {
            val result = JsonUtils.serializeStringList(listOf("a", "b"))

            assertEquals(2, result.length())
            assertEquals("a", result.getString(0))
            assertEquals("b", result.getString(1))
        }

        @Test
        fun `when serializing an optional string list, then null and empty entries are filtered out`() {
            val result = JsonUtils.serializeOptStringList(listOf("a", null, "", "b"))

            assertEquals(2, result?.length())
            assertEquals("a", result?.getString(0))
            assertEquals("b", result?.getString(1))
        }

        @Test
        fun `when serializing a null optional string list, then null is returned`() {
            assertNull(JsonUtils.serializeOptStringList(null))
        }
    }

    @Nested
    inner class IntListTest {

        @Test
        fun `when parsing an optional int list, then all entries are returned`() {
            val jsonObject = JSONObject("""{"array":[1,2,3]}""")

            assertEquals(listOf(1, 2, 3), jsonObject.optIntList("array"))
        }

        @Test
        fun `when the optional int list holds a non int entry, then that entry is skipped`() {
            val jsonArray = JSONArray("""[1,"a",3]""")

            assertEquals(listOf(1, 3), JsonUtils.parseOptIntegerList(jsonArray))
        }

        @Test
        fun `when parsing a null optional int list, then null is returned`() {
            assertNull(JsonUtils.parseOptIntegerList(null))
        }

        @Test
        fun `when the optional int list key does not exist, then null is returned`() {
            assertNull(JSONObject().optIntList("array"))
        }

        @Test
        fun `when serializing an optional int list, then all entries are kept`() {
            val result = JsonUtils.serializeOptIntegerList(listOf(1, 2))

            assertEquals(2, result?.length())
            assertEquals(1, result?.getInt(0))
            assertEquals(2, result?.getInt(1))
        }

        @Test
        fun `when serializing a null optional int list, then null is returned`() {
            assertNull(JsonUtils.serializeOptIntegerList(null))
        }
    }

    @Nested
    inner class MapTest {

        @Test
        fun `when getting a map, then the string entries are returned`() {
            val jsonObject = JSONObject("""{"map":{"a":"1","b":"2"}}""")

            assertEquals(mapOf("a" to "1", "b" to "2"), jsonObject.getMapOrNull("map"))
        }

        @Test
        fun `when the map holds a non string value, then that entry is dropped`() {
            val jsonObject = JSONObject("""{"map":{"a":"1","b":2}}""")

            assertEquals(mapOf("a" to "1"), jsonObject.getMapOrNull("map"))
        }

        @Test
        fun `when the map is null, then null is returned`() {
            val jsonObject = JSONObject("""{"map":null}""")

            assertNull(jsonObject.getMapOrNull("map"))
        }

        @Test
        fun `when the map key does not exist, then null is returned`() {
            assertNull(JSONObject().getMapOrNull("map"))
        }

        @Test
        fun `when converting a json object to a model map, then each value is deserialized`() {
            val jsonObject = JSONObject("""{"card":{"value":"a"},"ideal":{"value":"b"}}""")

            val result = jsonObject.jsonToMap(TestModelObject.SERIALIZER)

            assertEquals(mapOf("card" to TestModelObject("a"), "ideal" to TestModelObject("b")), result)
        }

        @Test
        fun `when converting a json object with a non object value, then that key is dropped`() {
            val jsonObject = JSONObject("""{"card":{"value":"a"},"ideal":"not an object"}""")

            val result = jsonObject.jsonToMap(TestModelObject.SERIALIZER)

            assertEquals(mapOf("card" to TestModelObject("a")), result)
        }
    }

    @Nested
    inner class ToStringPrettyTest {

        @Test
        fun `when a json object is printed, then the output is indented`() {
            // A single entry is printed on one line by org json, so two entries are needed here.
            val result = JSONObject("""{"key":"value","other":"value"}""").toStringPretty()

            assertTrue(result.contains("\n"), "Expected indented output but was: $result")
        }

        @Test
        fun `when a json array is printed, then the output is indented`() {
            val result = JSONArray("""["a","b"]""").toStringPretty()

            assertTrue(result.contains("\n"), "Expected indented output but was: $result")
        }
    }
}
