/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.model.JsonUtils
import org.json.JSONObject

class BacsDirectDebitPaymentMethod: PaymentMethodDetails() {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BacsDirectDebitPaymentMethod> = Creator(BacsDirectDebitPaymentMethod::class.java)

        @JvmStatic
        val SERIALIZER: Serializer<BacsDirectDebitPaymentMethod> = object : Serializer<BacsDirectDebitPaymentMethod> {

            override fun serialize(modelObject: BacsDirectDebitPaymentMethod): JSONObject {
                val jsonObject = JSONObject()
                //TODO serialize
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): BacsDirectDebitPaymentMethod {
                // TODO deserialize
                return BacsDirectDebitPaymentMethod()
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }
}