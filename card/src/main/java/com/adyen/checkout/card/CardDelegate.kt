/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/12/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.api.PublicKeyConnection
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.validation.ValidatedField
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONException
import java.io.IOException

private val TAG = LogUtil.getTag()
private const val CONNECTION_RETRIES = 3

abstract class CardDelegate(protected val cardConfiguration: CardConfiguration) : PaymentMethodDelegate {

    abstract fun validateCardNumber(cardNumber: String): ValidatedField<String>
    abstract fun validateExpiryDate(expiryDate: ExpiryDate): ValidatedField<ExpiryDate>
    abstract fun validateSecurityCode(securityCode: String, cardType: CardType? = null): ValidatedField<String>

    fun validateHolderName(holderName: String): ValidatedField<String> {
        return if (cardConfiguration.isHolderNameRequired && holderName.isBlank()) {
            ValidatedField(holderName, ValidatedField.Validation.INVALID)
        } else {
            ValidatedField(holderName, ValidatedField.Validation.VALID)
        }
    }

    abstract fun isCvcHidden(): Boolean
    abstract fun requiresInput(): Boolean
    abstract fun isHolderNameRequired(): Boolean

    suspend fun fetchPublicKey(): String {
        return if (cardConfiguration.publicKey.isNotEmpty() && CardValidationUtils.isPublicKeyValid(cardConfiguration.publicKey)) {
            Logger.d(TAG, "returning configuration publicKey")
            cardConfiguration.publicKey
        } else {
            Logger.d(TAG, "fetching publicKey from API")
            repeat(CONNECTION_RETRIES) {
                try {
                    return PublicKeyConnection(cardConfiguration.environment, cardConfiguration.clientKey).suspendedCall()
                } catch (e: IOException) {
                    Logger.e(TAG, "PublicKeyConnection Failed", e)
                } catch (e: JSONException) {
                    Logger.e(TAG, "PublicKeyConnection unexpected result", e)
                }
            }
            ""
        }
    }
}
