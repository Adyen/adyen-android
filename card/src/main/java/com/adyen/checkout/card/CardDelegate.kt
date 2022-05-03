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
import com.adyen.checkout.components.base.AddressVisibility
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.ui.FieldState
import kotlinx.coroutines.CoroutineScope

@Suppress("TooManyFunctions")
abstract class CardDelegate(
    protected val cardConfiguration: CardConfiguration,
    private val publicKeyRepository: PublicKeyRepository
) : PaymentMethodDelegate {

    protected val noCvcBrands: Set<CardType> = hashSetOf(CardType.BCMC)

    abstract fun validateCardNumber(cardNumber: String, enableLuhnCheck: Boolean, isBrandSupported: Boolean): FieldState<String>
    abstract fun validateExpiryDate(expiryDate: ExpiryDate, expiryDatePolicy: Brand.FieldPolicy?): FieldState<ExpiryDate>
    abstract fun validateSecurityCode(securityCode: String, cardType: DetectedCardType? = null): FieldState<String>
    abstract fun validateHolderName(holderName: String): FieldState<String>
    abstract fun validateSocialSecurityNumber(socialSecurityNumber: String): FieldState<String>
    abstract fun validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber: String): FieldState<String>
    abstract fun validateKcpCardPassword(kcpCardPassword: String): FieldState<String>
    abstract fun validateAddress(addressInputModel: AddressInputModel, addressFormUIState: AddressFormUIState): AddressOutputData
    abstract fun isCvcHidden(): Boolean
    abstract fun isSocialSecurityNumberRequired(): Boolean
    abstract fun isKCPAuthRequired(): Boolean
    abstract fun requiresInput(): Boolean
    abstract fun isHolderNameRequired(): Boolean
    abstract fun getAddressFormUIState(addressConfiguration: AddressConfiguration?, addressVisibility: AddressVisibility): AddressFormUIState
    abstract fun isAddressRequired(addressFormUIState: AddressFormUIState): Boolean
    abstract fun detectCardType(cardNumber: String, publicKey: String?, coroutineScope: CoroutineScope): List<DetectedCardType>
    abstract fun getFundingSource(): String?
    abstract fun getInstallmentOptions(
        installmentConfiguration: InstallmentConfiguration?,
        cardType: CardType?,
        isCardTypeReliable: Boolean
    ): List<InstallmentModel>

    suspend fun fetchPublicKey(): String {
        return publicKeyRepository.fetchPublicKey(
            environment = cardConfiguration.environment,
            clientKey = cardConfiguration.clientKey
        )
    }
}
