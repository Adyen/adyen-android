/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */
package com.adyen.checkout.core.internal.data.model

import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeAndDecodeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeModel
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeAndEncodeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOptList
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ModelUtilsTest {

    @Test
    fun `when deserializeModel is called, then model is deserialized`() {
        val jsonObject = JSONObject()

        // Verify is deserializeModel is able to get the Serializer by the class
        val parsedResult = deserializeModel(jsonObject, MockModelObject::class.java)
        assertNotNull(parsedResult)
    }

    @Test
    fun `when deserializeOpt is called for non null value, then model is deserialized`() {
        val jsonObject = JSONObject()
        val parsedResult = deserializeOpt(jsonObject, MockModelObject.SERIALIZER)
        assertNotNull(parsedResult)
    }

    @Test
    fun `when deserializeOpt is called for null, then null is returned`() {
        val parsedResult = deserializeOpt(null, MockModelObject.SERIALIZER)
        assertNull(parsedResult)
    }

    @Test
    fun `when deserializeAndDecodeOpt is called for non null value, then model is deserialized`() {
        val mockModelObject = MockModelObject("test", 123)
        val encodedResult = serializeAndEncodeOpt(mockModelObject, MockModelObject.SERIALIZER)

        val decodedResult = deserializeAndDecodeOpt(encodedResult, MockModelObject.SERIALIZER)

        assertNotNull(decodedResult)
        assertEquals(mockModelObject, decodedResult)
    }

    @Test
    fun `when deserializeAndDecodeOpt is called for null, then null is returned`() {
        val decodedResult = deserializeAndDecodeOpt(null, MockModelObject.SERIALIZER)

        assertNull(decodedResult)
    }

    @Test
    fun `when deserializeAndDecodeOpt is called for empty string, then null is returned`() {
        val decodedResult = deserializeAndDecodeOpt("", MockModelObject.SERIALIZER)

        assertNull(decodedResult)
    }

    @Test
    fun `when deserializeOptList is called for non null value, then model is deserialized`() {
        val jsonArray = JSONArray()
        val modelList = deserializeOptList(jsonArray, MockModelObject.SERIALIZER)
        assertNotNull(modelList)
    }

    @Test
    fun `when deserializeOptList is called for null, then null is returned`() {
        val modelList = deserializeOptList(null, MockModelObject.SERIALIZER)
        assertNull(modelList)
    }

    @Test
    fun `when serializeOpt is called for non null value, then model is serialized`() {
        val mockModelObject = MockModelObject()
        val jsonObject = serializeOpt(mockModelObject, MockModelObject.SERIALIZER)
        assertNotNull(jsonObject)
    }

    @Test
    fun `when serializeOpt is called for null, then null is returned`() {
        val jsonObject = serializeOpt(null, MockModelObject.SERIALIZER)
        assertNull(jsonObject)
    }

    @Test
    fun `when serializeAndEncodeOpt is called for non null value, then model is serialized and encoded`() {
        val mockModelObject = MockModelObject("test", 123)

        val result = serializeAndEncodeOpt(mockModelObject, MockModelObject.SERIALIZER)

        assertNotNull(result)
        assertTrue(result is String && result.isNotBlank())
    }

    @Test
    fun `when serializeAndEncodeOpt is called for null, then null is returned`() {
        val result = serializeAndEncodeOpt(null, MockModelObject.SERIALIZER)

        assertNull(result)
    }

    @Test
    fun `when serializeOptList is called for non null value, then model is serialized`() {
        val modelObjectList: MutableList<MockModelObject> = ArrayList()
        modelObjectList.add(MockModelObject())
        val jsonArray = serializeOptList(modelObjectList, MockModelObject.SERIALIZER)
        assertNotNull(jsonArray)
        assertTrue(!jsonArray!!.isNull(0))
    }

    @Test
    fun `when serializeOptList is called for null, then null is returned`() {
        val jsonArray = serializeOptList(null, MockModelObject.SERIALIZER)
        assertNull(jsonArray)
    }
}
