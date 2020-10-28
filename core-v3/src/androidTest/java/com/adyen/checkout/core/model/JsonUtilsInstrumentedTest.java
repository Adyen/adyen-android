/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class JsonUtilsInstrumentedTest {

    // These should match the same values from the class, don't want to expose the variables
    public static final int FLAG_NULL = 0;
    public static final int FLAG_NON_NULL = FLAG_NULL + 1;

    @Test
    public void writeToParcel_Pass_WriteJson() throws JSONException {
        Parcel parcel = Parcel.obtain();
        JSONObject jsonObject = new JSONObject();

        JsonUtils.writeToParcel(parcel, jsonObject);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        // Assert the nullness flag
        int nullness = parcel.readInt();
        Assert.assertEquals(nullness, FLAG_NON_NULL);

        // Assert the String result
        String resultJsonString = parcel.readString();
        Assert.assertNotNull(resultJsonString);
        Assert.assertTrue(!resultJsonString.isEmpty());

        // Assert the Json can be parsed back to JSONObject
        JSONObject resultJsonObject = new JSONObject(resultJsonString);
        Assert.assertEquals(jsonObject.toString(), resultJsonObject.toString());

    }

    @Test
    public void writeToParcel_Pass_WriteNull() {
        Parcel parcel = Parcel.obtain();

        JsonUtils.writeToParcel(parcel, null);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        // Assert the nullness flag
        int nullness = parcel.readInt();
        Assert.assertEquals(nullness, FLAG_NULL);
    }

    @Test
    public void readFromParcel_Pass_ReadJson() throws JSONException {

        Parcel parcel = Parcel.obtain();
        JSONObject jsonObject = new JSONObject();

        // Write JSON to parcel
        parcel.writeInt(FLAG_NON_NULL);
        parcel.writeString(jsonObject.toString());

        // Reset parcel for reading
        parcel.setDataPosition(0);

        JSONObject resultJsonObject = JsonUtils.readFromParcel(parcel);

        Assert.assertNotNull(resultJsonObject);
        Assert.assertEquals(jsonObject.toString(), resultJsonObject.toString());
    }

    @Test
    public void readFromParcel_Pass_ReadNull() throws JSONException {
        Parcel parcel = Parcel.obtain();

        // Write JSON to parcel
        parcel.writeInt(FLAG_NULL);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        JSONObject resultJsonObject = JsonUtils.readFromParcel(parcel);
        Assert.assertNull(resultJsonObject);
    }
}
