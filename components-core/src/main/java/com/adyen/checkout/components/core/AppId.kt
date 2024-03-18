/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

// TODO: Maybe create a mapper for UPI
// TODO: When making the payments call we get the redirect URL and if app is not installed we have to see what can be done.
// TODO: BCMC, WeChat, Twint might have the option to open the app on user's phone
// TODO: We only take UPI payment method from payment methods, we filter out the other UPIs. UPI gets priority.
// TODO: Icons will be handled like we do them for issuers list
// TODO: Check if we can change the text of the Continue button based on UPI mode
@Parcelize
data class AppId(
    var id: String? = null,
    var name: String? = null,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"

        @JvmField
        val SERIALIZER: Serializer<AppId> = object : Serializer<AppId> {
            override fun serialize(modelObject: AppId): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ID, modelObject.id)
                        putOpt(NAME, modelObject.name)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AppId::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AppId {
                return AppId(
                    id = jsonObject.getStringOrNull(ID),
                    name = jsonObject.getStringOrNull(NAME),
                )
            }
        }
    }
}
