/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/8/2026.
 */

package com.adyen.checkout.core.common.internal.model

import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptMap
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptMap
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ModelUtilsTest {

    @Nested
    inner class DeserializeOptTest {

        @Test
        fun `when the json object is not null, then it is deserialized`() {
            val jsonObject = JSONObject("""{"value":"a"}""")

            val result = deserializeOpt(jsonObject, TestModelObject.SERIALIZER)

            assertEquals(TestModelObject("a"), result)
        }

        @Test
        fun `when the json object is null, then the result is null`() {
            val result = deserializeOpt(null, TestModelObject.SERIALIZER)

            assertNull(result)
        }
    }

    @Nested
    inner class DeserializeOptListTest {

        @Test
        fun `when the json array holds objects, then all of them are deserialized in order`() {
            val jsonArray = JSONArray("""[{"value":"a"},{"value":"b"}]""")

            val result = deserializeOptList(jsonArray, TestModelObject.SERIALIZER)

            assertEquals(listOf(TestModelObject("a"), TestModelObject("b")), result)
        }

        @Test
        fun `when the json array holds an entry that is not an object, then that entry is skipped`() {
            val jsonArray = JSONArray("""[{"value":"a"},"not an object"]""")

            val result = deserializeOptList(jsonArray, TestModelObject.SERIALIZER)

            assertEquals(listOf(TestModelObject("a")), result)
        }

        @Test
        fun `when the json array is empty, then the result is an empty list`() {
            val result = deserializeOptList(JSONArray(), TestModelObject.SERIALIZER)

            assertEquals(emptyList<TestModelObject>(), result)
        }

        @Test
        fun `when the json array is null, then the result is null`() {
            val result = deserializeOptList(null, TestModelObject.SERIALIZER)

            assertNull(result)
        }

        @Test
        fun `when the result is mutated, then it throws because the list is unmodifiable`() {
            val jsonArray = JSONArray("""[{"value":"a"}]""")

            val result = deserializeOptList(jsonArray, TestModelObject.SERIALIZER)

            @Suppress("UNCHECKED_CAST")
            assertThrows(UnsupportedOperationException::class.java) {
                (result as MutableList<TestModelObject>).add(TestModelObject("b"))
            }
        }
    }

    @Nested
    inner class DeserializeOptMapTest {

        @Test
        fun `when the json object holds nested objects, then they are deserialized per key`() {
            val jsonObject = JSONObject("""{"card":{"value":"a"},"ideal":{"value":"b"}}""")

            val result = deserializeOptMap(jsonObject, TestModelObject.SERIALIZER)

            val expected = mapOf("card" to TestModelObject("a"), "ideal" to TestModelObject("b"))
            assertEquals(expected, result)
        }

        @Test
        fun `when the json object is null, then the result is null`() {
            val result = deserializeOptMap(null, TestModelObject.SERIALIZER)

            assertNull(result)
        }

        @Test
        fun `when the result is mutated, then it throws because the map is unmodifiable`() {
            val jsonObject = JSONObject("""{"card":{"value":"a"}}""")

            val result = deserializeOptMap(jsonObject, TestModelObject.SERIALIZER)

            @Suppress("UNCHECKED_CAST")
            assertThrows(UnsupportedOperationException::class.java) {
                (result as MutableMap<String, TestModelObject?>)["ideal"] = TestModelObject("b")
            }
        }
    }

    @Nested
    inner class SerializeOptTest {

        @Test
        fun `when the model is not null, then it is serialized`() {
            val result = serializeOpt(TestModelObject("a"), TestModelObject.SERIALIZER)

            assertEquals("a", result?.getString(TestModelObject.VALUE))
        }

        @Test
        fun `when the model is null, then the result is null`() {
            val result = serializeOpt(null, TestModelObject.SERIALIZER)

            assertNull(result)
        }
    }

    @Nested
    inner class SerializeOptListTest {

        @Test
        fun `when the list holds models, then all of them are serialized in order`() {
            val modelList = listOf(TestModelObject("a"), TestModelObject("b"))

            val result = serializeOptList(modelList, TestModelObject.SERIALIZER)

            assertEquals(2, result?.length())
            assertEquals("a", result?.getJSONObject(0)?.getString(TestModelObject.VALUE))
            assertEquals("b", result?.getJSONObject(1)?.getString(TestModelObject.VALUE))
        }

        @Test
        fun `when the list is empty, then the result is null instead of an empty array`() {
            val result = serializeOptList(emptyList(), TestModelObject.SERIALIZER)

            assertNull(result)
        }

        @Test
        fun `when the list is null, then the result is null`() {
            val result = serializeOptList(null, TestModelObject.SERIALIZER)

            assertNull(result)
        }
    }

    @Nested
    inner class SerializeOptMapTest {

        @Test
        fun `when the map holds models, then they are serialized per key`() {
            val modelMap = mapOf("card" to TestModelObject("a"), "ideal" to TestModelObject("b"))

            val result = serializeOptMap(modelMap, TestModelObject.SERIALIZER)

            assertEquals("a", result?.getJSONObject("card")?.getString(TestModelObject.VALUE))
            assertEquals("b", result?.getJSONObject("ideal")?.getString(TestModelObject.VALUE))
        }

        @Test
        fun `when a value is null, then the key is left out of the json object`() {
            val modelMap = mapOf("card" to TestModelObject("a"), "ideal" to null)

            val result = serializeOptMap(modelMap, TestModelObject.SERIALIZER)

            assertEquals(1, result?.length())
            assertEquals("a", result?.getJSONObject("card")?.getString(TestModelObject.VALUE))
        }

        @Test
        fun `when the map is empty, then the result is an empty json object`() {
            val result = serializeOptMap(emptyMap(), TestModelObject.SERIALIZER)

            assertEquals(0, result?.length())
        }

        @Test
        fun `when the map is null, then the result is null`() {
            val result = serializeOptMap(null, TestModelObject.SERIALIZER)

            assertNull(result)
        }
    }
}
