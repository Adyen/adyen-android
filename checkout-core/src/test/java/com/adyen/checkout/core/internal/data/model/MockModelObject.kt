/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */
package com.adyen.checkout.core.internal.data.model

import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
internal data class MockModelObject(
    var firstField: String? = null,
    var secondField: Int? = null
) : ModelObject() {

    companion object {
        private const val FIRST_FIELD = "firstField"
        private const val SECOND_FIELD = "secondField"

        @JvmField
        val SERIALIZER: Serializer<MockModelObject> = object : Serializer<MockModelObject> {
            override fun serialize(modelObject: MockModelObject): JSONObject {
                return JSONObject().apply {
                    putOpt(FIRST_FIELD, modelObject.firstField)
                    putOpt(SECOND_FIELD, modelObject.secondField)
                }
            }

            override fun deserialize(jsonObject: JSONObject): MockModelObject {
                return MockModelObject(
                    firstField = jsonObject.getStringOrNull(FIRST_FIELD),
                    secondField = jsonObject.getIntOrNull(SECOND_FIELD),
                )
            }
        }
    }
}
