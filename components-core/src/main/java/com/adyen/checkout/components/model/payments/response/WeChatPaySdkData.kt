/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/10/2019.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class WeChatPaySdkData(
    var appid: String? = null,
    var noncestr: String? = null,
    var packageValue: String? = null,
    var partnerid: String? = null,
    var prepayid: String? = null,
    var sign: String? = null,
    var timestamp: String? = null,
) : SdkData() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val APP_ID = "appid"
        private const val NONCE_STR = "noncestr"
        private const val PACKAGE_VALUE = "packageValue"
        private const val PARTNER_ID = "partnerid"
        private const val PREPAY_ID = "prepayid"
        private const val SIGN = "sign"
        private const val TIMESTAMP = "timestamp"

        @JvmField
        val CREATOR = Creator(WeChatPaySdkData::class.java)

        @JvmField
        val SERIALIZER: Serializer<WeChatPaySdkData> = object : Serializer<WeChatPaySdkData> {
            override fun serialize(modelObject: WeChatPaySdkData): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(APP_ID, modelObject.appid)
                        putOpt(NONCE_STR, modelObject.noncestr)
                        putOpt(PACKAGE_VALUE, modelObject.packageValue)
                        putOpt(PARTNER_ID, modelObject.partnerid)
                        putOpt(PREPAY_ID, modelObject.prepayid)
                        putOpt(SIGN, modelObject.sign)
                        putOpt(TIMESTAMP, modelObject.timestamp)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(WeChatPaySdkData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): WeChatPaySdkData {
                return WeChatPaySdkData(
                    appid = jsonObject.getStringOrNull(APP_ID),
                    noncestr = jsonObject.getStringOrNull(NONCE_STR),
                    packageValue = jsonObject.getStringOrNull(PACKAGE_VALUE),
                    partnerid = jsonObject.getStringOrNull(PARTNER_ID),
                    prepayid = jsonObject.getStringOrNull(PREPAY_ID),
                    sign = jsonObject.getStringOrNull(SIGN),
                    timestamp = jsonObject.getStringOrNull(TIMESTAMP),
                )
            }
        }
    }
}
