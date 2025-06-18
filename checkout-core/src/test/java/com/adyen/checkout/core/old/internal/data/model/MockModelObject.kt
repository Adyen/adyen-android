/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */
package com.adyen.checkout.core.old.internal.data.model

import android.os.Parcel
import org.json.JSONObject

internal class MockModelObject : ModelObject() {
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
