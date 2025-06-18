/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.components.core.action

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class VoucherAction(
    override var type: String? = null,
    override var paymentData: String? = null,
    override var paymentMethodType: String? = null,
    var entity: String? = null,
    var surcharge: Amount? = null,
    var initialAmount: Amount? = null,
    var totalAmount: Amount? = null,
    var issuer: String? = null,
    var expiresAt: String? = null,
    var reference: String? = null,
    var collectionInstitutionNumber: String? = null,
    var maskedTelephoneNumber: String? = null,
    var alternativeReference: String? = null,
    var merchantName: String? = null,
    var merchantReference: String? = null,
    // TODO remove url when it's fixed from backend side
    var url: String? = null,
    var downloadUrl: String? = null,
    var instructionsUrl: String? = null,
) : Action() {

    companion object {
        const val ACTION_TYPE = ActionTypes.VOUCHER
        private const val ENTITY = "entity"
        private const val SURCHARGE = "surcharge"
        private const val INITIAL_AMOUNT = "initialAmount"
        private const val TOTAL_AMOUNT = "totalAmount"
        private const val ISSUER = "issuer"
        private const val EXPIRES_AT = "expiresAt"
        private const val REFERENCE = "reference"
        private const val COLLECTION_INSTITUTION_NUMBER = "collectionInstitutionNumber"
        private const val MASKED_TELEPHONE_NUMBER = "maskedTelephoneNumber"
        private const val ALTERNATIVE_REFERENCE = "alternativeReference"
        private const val MERCHANT_NAME = "merchantName"
        private const val MERCHANT_REFERENCE = "merchantReference"
        private const val URL = "url"
        private const val DOWNLOAD_URL = "downloadUrl"
        private const val INSTRUCTIONS_URL = "instructionsUrl"

        @JvmField
        val SERIALIZER: Serializer<VoucherAction> = object : Serializer<VoucherAction> {
            override fun serialize(modelObject: VoucherAction): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(PAYMENT_METHOD_TYPE, modelObject.paymentMethodType)
                        putOpt(ENTITY, modelObject.entity)
                        putOpt(SURCHARGE, serializeOpt(modelObject.surcharge, Amount.SERIALIZER))
                        putOpt(INITIAL_AMOUNT, serializeOpt(modelObject.initialAmount, Amount.SERIALIZER))
                        putOpt(TOTAL_AMOUNT, serializeOpt(modelObject.totalAmount, Amount.SERIALIZER))
                        putOpt(ISSUER, modelObject.issuer)
                        putOpt(EXPIRES_AT, modelObject.expiresAt)
                        putOpt(REFERENCE, modelObject.reference)
                        putOpt(COLLECTION_INSTITUTION_NUMBER, modelObject.collectionInstitutionNumber)
                        putOpt(MASKED_TELEPHONE_NUMBER, modelObject.maskedTelephoneNumber)
                        putOpt(ALTERNATIVE_REFERENCE, modelObject.alternativeReference)
                        putOpt(MERCHANT_NAME, modelObject.merchantName)
                        putOpt(MERCHANT_REFERENCE, modelObject.merchantReference)
                        putOpt(URL, modelObject.url)
                        putOpt(DOWNLOAD_URL, modelObject.downloadUrl)
                        putOpt(INSTRUCTIONS_URL, modelObject.instructionsUrl)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(VoucherAction::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): VoucherAction {
                return VoucherAction(
                    type = jsonObject.getStringOrNull(TYPE),
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    paymentMethodType = jsonObject.getStringOrNull(PAYMENT_METHOD_TYPE),
                    entity = jsonObject.getStringOrNull(ENTITY),
                    surcharge = deserializeOpt(jsonObject.optJSONObject(SURCHARGE), Amount.SERIALIZER),
                    initialAmount = deserializeOpt(jsonObject.optJSONObject(INITIAL_AMOUNT), Amount.SERIALIZER),
                    totalAmount = deserializeOpt(jsonObject.optJSONObject(TOTAL_AMOUNT), Amount.SERIALIZER),
                    issuer = jsonObject.getStringOrNull(ISSUER),
                    expiresAt = jsonObject.getStringOrNull(EXPIRES_AT),
                    reference = jsonObject.getStringOrNull(REFERENCE),
                    collectionInstitutionNumber = jsonObject.getStringOrNull(COLLECTION_INSTITUTION_NUMBER),
                    maskedTelephoneNumber = jsonObject.getStringOrNull(MASKED_TELEPHONE_NUMBER),
                    alternativeReference = jsonObject.getStringOrNull(ALTERNATIVE_REFERENCE),
                    merchantName = jsonObject.getStringOrNull(MERCHANT_NAME),
                    merchantReference = jsonObject.getStringOrNull(MERCHANT_REFERENCE),
                    url = jsonObject.getStringOrNull(URL),
                    downloadUrl = jsonObject.getStringOrNull(DOWNLOAD_URL),
                    instructionsUrl = jsonObject.getStringOrNull(INSTRUCTIONS_URL),
                )
            }
        }
    }
}
