/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */

package com.adyen.checkout.core.model;

import static org.junit.Assert.assertNotNull;

import com.adyen.checkout.core.mock.MockModelObject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ModelUtilsTest {

    @Test
    public void parseModel_Pass_ParseMockedModelByClass() {
        JSONObject jsonObject = new JSONObject();

        // Verify is deserializeModel is able to get the Serializer by the class
        MockModelObject parsedResult =  ModelUtils.deserializeModel(jsonObject, MockModelObject.class);
        assertNotNull(parsedResult);
    }

    @Test
    public void parseOpt_Pass_ParseMockedModel() {
        JSONObject jsonObject = new JSONObject();
        MockModelObject parsedResult =  ModelUtils.deserializeOpt(jsonObject, MockModelObject.SERIALIZER);
        Assert.assertNotNull(parsedResult);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void parseOpt_Pass_ParseNull() {
        MockModelObject parsedResult =  ModelUtils.deserializeOpt(null, MockModelObject.SERIALIZER);
        Assert.assertNull(parsedResult);
    }

    @Test
    public void parseOptList_Pass_ParseMockedModel() {
        JSONArray jsonArray = new JSONArray();
        List<MockModelObject> modelList =  ModelUtils.deserializeOptList(jsonArray, MockModelObject.SERIALIZER);
        Assert.assertNotNull(modelList);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void parseOptList_Pass_ParseNull() {
        List<MockModelObject> modelList =  ModelUtils.deserializeOptList(null, MockModelObject.SERIALIZER);
        Assert.assertNull(modelList);
    }

    @Test
    public void serializeOpt_Pass_SerializeMockedModel() {
        MockModelObject mockModelObject = new MockModelObject();
        JSONObject jsonObject =  ModelUtils.serializeOpt(mockModelObject, MockModelObject.SERIALIZER);
        Assert.assertNotNull(jsonObject);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void serializeOpt_Pass_SerializeNull() {
        JSONObject jsonObject =  ModelUtils.serializeOpt(null, MockModelObject.SERIALIZER);
        Assert.assertNull(jsonObject);
    }

    @Test
    public void serializeOptList_Pass_SerializeMockedModelList() {
        List<MockModelObject> modelObjectList = new ArrayList<>();
        modelObjectList.add(new MockModelObject());

        JSONArray jsonArray = ModelUtils.serializeOptList(modelObjectList, MockModelObject.SERIALIZER);
        Assert.assertNotNull(jsonArray);
        Assert.assertTrue(!jsonArray.isNull(0));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void serializeOptList_Pass_SerializeNull() {
        JSONArray jsonArray = ModelUtils.serializeOptList(null, MockModelObject.SERIALIZER);
        Assert.assertNull(jsonArray);
    }
}
