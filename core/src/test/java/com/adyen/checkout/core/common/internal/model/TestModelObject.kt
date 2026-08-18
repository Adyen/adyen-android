/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/8/2026.
 */

package com.adyen.checkout.core.common.internal.model

import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Minimal [ModelObject] used to exercise [ModelUtils] and the [JsonUtils] map helpers. It carries a single
 * [value] so serialization can be asserted on content instead of only on nullability.
 */
@Parcelize
internal data class TestModelObject(val value: String = "") : ModelObject() {

    companion object {
        const val VALUE = "value"

        @JvmField
        val SERIALIZER: Serializer<TestModelObject> = object : Serializer<TestModelObject> {
            override fun serialize(modelObject: TestModelObject): JSONObject {
                return JSONObject().apply { put(VALUE, modelObject.value) }
            }

            override fun deserialize(jsonObject: JSONObject): TestModelObject {
                return TestModelObject(value = jsonObject.optString(VALUE))
            }
        }
    }
}
