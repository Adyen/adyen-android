/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/11/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.repository.BinLookupRepository
import com.adyen.checkout.card.repository.PublicKeyRepository
import com.adyen.checkout.components.base.AddressVisibility
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class NewCardDelegate(
    private val paymentMethod: PaymentMethod,
    cardConfiguration: CardConfiguration,
    private val binLookupRepository: BinLookupRepository,
    publicKeyRepository: PublicKeyRepository
) : CardDelegate(cardConfiguration, publicKeyRepository) {

    private val _binLookupFlow: MutableSharedFlow<List<DetectedCardType>> = MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
    internal val binLookupFlow: Flow<List<DetectedCardType>> = _binLookupFlow

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun validateCardNumber(cardNumber: String): FieldState<String> {
        return CardValidationUtils.validateCardNumber(cardNumber)
    }

    override fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return CardValidationUtils.validateExpiryDate(expiryDate)
    }

    override fun validateSecurityCode(
        securityCode: String,
        cardType: DetectedCardType?
    ): FieldState<String> {
        return if (cardConfiguration.isHideCvc) {
            FieldState(
                securityCode,
                Validation.Valid
            )
        } else {
            CardValidationUtils.validateSecurityCode(securityCode, cardType)
        }
    }

    override fun validateHolderName(holderName: String): FieldState<String> {
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

    override fun validateSocialSecurityNumber(socialSecurityNumber: String): FieldState<String> {
        return if (isSocialSecurityNumberRequired()) {
            SocialSecurityNumberUtils.validateSocialSecurityNumber(socialSecurityNumber)
        } else {
            FieldState(socialSecurityNumber, Validation.Valid)
        }
    }

    override fun validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber: String): FieldState<String> {
        return if (isKCPAuthRequired()) {
            KcpValidationUtils.validateKcpBirthDateOrTaxNumber(kcpBirthDateOrTaxNumber)
        } else {
            FieldState(kcpBirthDateOrTaxNumber, Validation.Valid)
        }
    }

    override fun validateKcpCardPassword(kcpCardPassword: String): FieldState<String> {
        return if (isKCPAuthRequired()) {
            KcpValidationUtils.validateKcpCardPassword(kcpCardPassword)
        } else {
            FieldState(kcpCardPassword, Validation.Valid)
        }
    }

    override fun validatePostalCode(postalCode: String): FieldState<String> {
        val validation = if (postalCode.isNotEmpty()) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_card_postal_not_valid)
        }
        return FieldState(postalCode, validation)
    }

    override fun isCvcHidden(): Boolean {
        return cardConfiguration.isHideCvc
    }

    override fun isSocialSecurityNumberRequired(): Boolean {
        return cardConfiguration.socialSecurityNumberVisibility == SocialSecurityNumberVisibility.SHOW
    }

    override fun isKCPAuthRequired(): Boolean {
        return cardConfiguration.kcpAuthVisibility == KCPAuthVisibility.SHOW
    }

    override fun requiresInput(): Boolean {
        return true
    }

    override fun isHolderNameRequired(): Boolean {
        return cardConfiguration.isHolderNameRequired
    }

    override fun isPostalCodeRequired(): Boolean {
        return cardConfiguration.addressVisibility == AddressVisibility.POSTAL_CODE
    }

    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        coroutineScope: CoroutineScope
    ): List<DetectedCardType> {
        Logger.d(TAG, "detectCardType")
        if (binLookupRepository.isRequiredSize(cardNumber)) {
            if (binLookupRepository.contains(cardNumber)) {
                Logger.d(TAG, "Returning cashed result.")
                return binLookupRepository.get(cardNumber)
            }

            if (publicKey != null) {
                Logger.d(TAG, "Launching Bin Lookup")
                coroutineScope.launch {
                    val detectedCardTypes = binLookupRepository.fetch(cardNumber, publicKey, cardConfiguration)
                    Logger.d(TAG, "Emitting new detectedCardTypes")
                    _binLookupFlow.tryEmit(detectedCardTypes)
                }
            }
        }

        return detectCardLocally(cardNumber)
    }

    private fun detectCardLocally(cardNumber: String): List<DetectedCardType> {
        Logger.d(TAG, "detectCardLocally")
        if (cardNumber.isEmpty()) {
            return emptyList()
        }
        val supportedCardTypes = cardConfiguration.supportedCardTypes
        val estimateCardTypes = CardType.estimate(cardNumber)
        val detectedCardTypes = supportedCardTypes.filter { estimateCardTypes.contains(it) }
        return detectedCardTypes.map { localDetectedCard(it) }
    }

    private fun localDetectedCard(cardType: CardType): DetectedCardType {
        return DetectedCardType(
            cardType,
            isReliable = false,
            showExpiryDate = true,
            enableLuhnCheck = true,
            cvcPolicy = when {
                noCvcBrands.contains(cardType) -> Brand.CvcPolicy.HIDDEN
                else -> Brand.CvcPolicy.REQUIRED
            }
        )
    }
}
