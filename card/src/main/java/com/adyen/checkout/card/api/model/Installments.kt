package com.adyen.checkout.card.api.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class Installments(
    val plan: String? = null,
    val value: Int = 1
): ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val PLAN = "plan"
        private const val VALUE = "value"

        @JvmField
        val CREATOR: Parcelable.Creator<Installments> = Creator(Installments::class.java)

        @JvmStatic
        val SERIALIZER: Serializer<Installments> = object : Serializer<Installments> {
            override fun serialize(modelObject: Installments): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(PLAN, modelObject.plan)
                    jsonObject.putOpt(VALUE, modelObject.value)
                } catch (e: JSONException) {
                    throw ModelSerializationException(Installments::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): Installments {
                return try {
                    Installments(
                        plan = jsonObject.getStringOrNull(PLAN),
                        value = jsonObject.optInt(VALUE, 1)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(Installments::class.java, e)
                }
            }
        }

    }
}
