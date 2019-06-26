/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */

package com.adyen.checkout.core.model;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JsonUtilsTest {

    @Test
    public void parseOptStringList_Pass_ParseStringArray() {

        JSONArray jsonArray = new JSONArray();
        String testString = "Test";
        jsonArray.put(testString);

        List<String> stringList = JsonUtils.parseOptStringList(jsonArray);

        Assert.assertNotNull(stringList);
        String first = stringList.get(0);

        Assert.assertEquals(testString, first);
    }

    @Test
    public void parseOptStringList_Pass_ParseEmptyArray() {
        JSONArray jsonArray = new JSONArray();

        List<String> stringList = JsonUtils.parseOptStringList(jsonArray);

        Assert.assertNotNull(stringList);
        Assert.assertTrue(stringList.isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void parseOptStringList_Pass_ParseNull() {
        List<String> stringList = JsonUtils.parseOptStringList(null);
        Assert.assertNull(stringList);
    }

}
