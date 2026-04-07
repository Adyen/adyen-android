/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/3/2026.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class AppIdentifierInfo(
    val androidPackageId: String? = null,
) : ModelObject() {

    companion object {

        private const val ANDROID_PACKAGE_ID = "androidPackageId"

        @JvmField
        val SERIALIZER: Serializer<AppIdentifierInfo> = object : Serializer<AppIdentifierInfo> {

            override fun serialize(modelObject: AppIdentifierInfo): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ANDROID_PACKAGE_ID, modelObject.androidPackageId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AppIdentifierInfo::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AppIdentifierInfo {
                return AppIdentifierInfo(
                    androidPackageId = jsonObject.getStringOrNull(ANDROID_PACKAGE_ID),
                )
            }
        }
    }
}
