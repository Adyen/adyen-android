/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/3/2022.
 */
package com.adyen.checkout.sessions.model

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONException
import org.json.JSONObject

data class Session(
    val id: String,
    val sessionData: String?
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ID = "id"
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val CREATOR = Creator(Session::class.java)

        @JvmField
        val SERIALIZER: Serializer<Session> = object : Serializer<Session> {
            override fun serialize(modelObject: Session): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(ID, modelObject.id)
                        putOpt(SESSION_DATA, modelObject.sessionData)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(Session::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): Session {
                return Session(
                    id = jsonObject.optString(ID),
                    sessionData = jsonObject.optString(SESSION_DATA)
                )
            }
        }
    }
}
