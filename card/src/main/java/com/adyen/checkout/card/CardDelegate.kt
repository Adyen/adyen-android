/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/12/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.repository.PublicKeyRepository
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.ui.FieldState
import kotlinx.coroutines.CoroutineScope

@Suppress("TooManyFunctions")
abstract class CardDelegate(
    protected val cardConfiguration: CardConfiguration,
    private val publicKeyRepository: PublicKeyRepository
) : PaymentMethodDelegate {

    protected val noCvcBrands: Set<CardType> = hashSetOf(CardType.BCMC)

    abstract fun validateCardNumber(cardNumber: String, enableLuhnCheck: Boolean?): FieldState<String>
    abstract fun validateExpiryDate(expiryDate: ExpiryDate, expiryDatePolicy: Brand.FieldPolicy?): FieldState<ExpiryDate>
    abstract fun validateSecurityCode(securityCode: String, cardType: DetectedCardType? = null): FieldState<String>
    abstract fun validateHolderName(holderName: String): FieldState<String>
    abstract fun validateSocialSecurityNumber(socialSecurityNumber: String): FieldState<String>
    abstract fun validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber: String): FieldState<String>
    abstract fun validateKcpCardPassword(kcpCardPassword: String): FieldState<String>
    abstract fun isCvcHidden(): Boolean
    abstract fun isSocialSecurityNumberRequired(): Boolean
    abstract fun isKCPAuthRequired(): Boolean
    abstract fun requiresInput(): Boolean
    abstract fun isHolderNameRequired(): Boolean
    abstract fun detectCardType(cardNumber: String, publicKey: String?, coroutineScope: CoroutineScope): List<DetectedCardType>

    suspend fun fetchPublicKey(): String {
        return publicKeyRepository.fetchPublicKey(
            environment = cardConfiguration.environment,
            clientKey = cardConfiguration.clientKey
        )
    }
}
