/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */
package com.adyen.checkout.core.mock

import android.os.Parcel
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONObject

class MockModelObject : ModelObject() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        // empty
    }

    companion object {
        @JvmField
        val SERIALIZER: Serializer<MockModelObject> = object : Serializer<MockModelObject> {
            override fun serialize(modelObject: MockModelObject): JSONObject {
                return JSONObject()
            }

            override fun deserialize(jsonObject: JSONObject): MockModelObject {
                return MockModelObject()
            }
        }
    }
}
