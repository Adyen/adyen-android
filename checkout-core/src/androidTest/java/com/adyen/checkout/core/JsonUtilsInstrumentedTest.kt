/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */
package com.adyen.checkout.core

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adyen.checkout.core.model.JsonUtils.readFromParcel
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonUtilsInstrumentedTest {

    @Test
    @Throws(JSONException::class)
    fun writeToParcel_Pass_WriteJson() {
        val parcel = Parcel.obtain()
        val jsonObject = JSONObject()
        writeToParcel(parcel, jsonObject)

        // Reset parcel for reading
        parcel.setDataPosition(0)

        // Assert the nullness flag
        val nullness = parcel.readInt()
        Assert.assertEquals(nullness.toLong(), FLAG_NON_NULL.toLong())

        // Assert the String result
        val resultJsonString = parcel.readString()
        Assert.assertNotNull(resultJsonString)
        Assert.assertTrue(!resultJsonString!!.isEmpty())

        // Assert the Json can be parsed back to JSONObject
        val resultJsonObject = JSONObject(resultJsonString)
        Assert.assertEquals(jsonObject.toString(), resultJsonObject.toString())
    }

    @Test
    fun writeToParcel_Pass_WriteNull() {
        val parcel = Parcel.obtain()
        writeToParcel(parcel, null)

        // Reset parcel for reading
        parcel.setDataPosition(0)

        // Assert the nullness flag
        val nullness = parcel.readInt()
        Assert.assertEquals(nullness.toLong(), FLAG_NULL.toLong())
    }

    @Test
    @Throws(JSONException::class)
    fun readFromParcel_Pass_ReadJson() {
        val parcel = Parcel.obtain()
        val jsonObject = JSONObject()

        // Write JSON to parcel
        parcel.writeInt(FLAG_NON_NULL)
        parcel.writeString(jsonObject.toString())

        // Reset parcel for reading
        parcel.setDataPosition(0)
        val resultJsonObject = readFromParcel(parcel)
        Assert.assertNotNull(resultJsonObject)
        Assert.assertEquals(jsonObject.toString(), resultJsonObject.toString())
    }

    @Test
    @Throws(JSONException::class)
    fun readFromParcel_Pass_ReadNull() {
        val parcel = Parcel.obtain()

        // Write JSON to parcel
        parcel.writeInt(FLAG_NULL)

        // Reset parcel for reading
        parcel.setDataPosition(0)
        val resultJsonObject = readFromParcel(parcel)
        Assert.assertNull(resultJsonObject)
    }

    companion object {
        // These should match the same values from the class, don't want to expose the variables
        const val FLAG_NULL = 0
        const val FLAG_NON_NULL = FLAG_NULL + 1
    }
}
