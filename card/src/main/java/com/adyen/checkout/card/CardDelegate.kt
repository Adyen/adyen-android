/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/12/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.repository.PublicKeyRepository
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import kotlinx.coroutines.CoroutineScope

@Suppress("TooManyFunctions")
abstract class CardDelegate(
    protected val cardConfiguration: CardConfiguration,
    private val publicKeyRepository: PublicKeyRepository
) : PaymentMethodDelegate {

    protected val noCvcBrands: Set<CardType> = hashSetOf(CardType.BCMC)

    abstract fun validateCardNumber(cardNumber: String): FieldState<String>
    abstract fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate>
    abstract fun validateSecurityCode(securityCode: String, cardType: CardType? = null): FieldState<String>

    fun validateHolderName(holderName: String): FieldState<String> {
        return if (cardConfiguration.isHolderNameRequired && holderName.isBlank()) {
            FieldState(
                holderName,
                Validation.Invalid(R.string.checkout_holder_name_not_valid)
            )
        } else {
            FieldState(
                holderName,
                Validation.Valid
            )
        }
    }

    abstract fun isCvcHidden(): Boolean
    abstract fun requiresInput(): Boolean
    abstract fun isHolderNameRequired(): Boolean
    abstract fun detectCardType(cardNumber: String, publicKey: String, coroutineScope: CoroutineScope): List<DetectedCardType>

    suspend fun fetchPublicKey(): String {
        return publicKeyRepository.fetchPublicKey(
            environment = cardConfiguration.environment,
            clientKey = cardConfiguration.clientKey
        )
    }
}
