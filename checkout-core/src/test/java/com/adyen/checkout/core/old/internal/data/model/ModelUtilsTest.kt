/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */
package com.adyen.checkout.core.old.internal.data.model

import com.adyen.checkout.core.old.internal.data.model.ModelUtils.deserializeModel
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.serializeOptList
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ModelUtilsTest {

    @Test
    fun parseModel_Pass_ParseMockedModelByClass() {
        val jsonObject = JSONObject()

        // Verify is deserializeModel is able to get the Serializer by the class
        val parsedResult = deserializeModel(jsonObject, MockModelObject::class.java)
        assertNotNull(parsedResult)
    }

    @Test
    fun parseOpt_Pass_ParseMockedModel() {
        val jsonObject = JSONObject()
        val parsedResult = deserializeOpt(jsonObject, MockModelObject.SERIALIZER)
        assertNotNull(parsedResult)
    }

    @Test
    fun parseOpt_Pass_ParseNull() {
        val parsedResult = deserializeOpt(null, MockModelObject.SERIALIZER)
        assertNull(parsedResult)
    }

    @Test
    fun parseOptList_Pass_ParseMockedModel() {
        val jsonArray = JSONArray()
        val modelList = deserializeOptList(jsonArray, MockModelObject.SERIALIZER)
        assertNotNull(modelList)
    }

    @Test
    fun parseOptList_Pass_ParseNull() {
        val modelList = deserializeOptList(null, MockModelObject.SERIALIZER)
        assertNull(modelList)
    }

    @Test
    fun serializeOpt_Pass_SerializeMockedModel() {
        val mockModelObject = MockModelObject()
        val jsonObject = serializeOpt(mockModelObject, MockModelObject.SERIALIZER)
        assertNotNull(jsonObject)
    }

    @Test
    fun serializeOpt_Pass_SerializeNull() {
        val jsonObject = serializeOpt(null, MockModelObject.SERIALIZER)
        assertNull(jsonObject)
    }

    @Test
    fun serializeOptList_Pass_SerializeMockedModelList() {
        val modelObjectList: MutableList<MockModelObject> = ArrayList()
        modelObjectList.add(MockModelObject())
        val jsonArray = serializeOptList(modelObjectList, MockModelObject.SERIALIZER)
        assertNotNull(jsonArray)
        assertTrue(!jsonArray!!.isNull(0))
    }

    @Test
    fun serializeOptList_Pass_SerializeNull() {
        val jsonArray = serializeOptList(null, MockModelObject.SERIALIZER)
        assertNull(jsonArray)
    }
}
