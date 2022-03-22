/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/11/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.card.repository.BinLookupRepository
import com.adyen.checkout.components.base.AddressVisibility
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PublicKeyRepository
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
private const val DEBIT_FUNDING_SOURCE = "debit"

@Suppress("TooManyFunctions")
class NewCardDelegate(
    private val paymentMethod: PaymentMethod,
    cardConfiguration: CardConfiguration,
    private val binLookupRepository: BinLookupRepository,
    publicKeyRepository: PublicKeyRepository,
    private val addressDelegate: AddressDelegate,
    private val cardValidationMapper: CardValidationMapper
) : CardDelegate(cardConfiguration, publicKeyRepository) {

    private val _binLookupFlow: MutableSharedFlow<List<DetectedCardType>> =
        MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
    internal val binLookupFlow: Flow<List<DetectedCardType>> = _binLookupFlow

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun validateCardNumber(
        cardNumber: String,
        enableLuhnCheck: Boolean,
        isBrandSupported: Boolean
    ): FieldState<String> {
        val validation = CardValidationUtils.validateCardNumber(cardNumber, enableLuhnCheck, isBrandSupported)
        return cardValidationMapper.mapCardNumberValidation(cardNumber, validation)
    }

    override fun validateExpiryDate(
        expiryDate: ExpiryDate,
        expiryDatePolicy: Brand.FieldPolicy?
    ): FieldState<ExpiryDate> {
        return CardValidationUtils.validateExpiryDate(expiryDate, expiryDatePolicy)
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
        val validation = when {
            isPostalCodeRequired() -> {
                if (postalCode.isNotEmpty()) {
                    Validation.Valid
                } else {
                    Validation.Invalid(R.string.checkout_card_postal_not_valid)
                }
            }
            else -> Validation.Valid
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

    override fun getFundingSource(): String? {
        return paymentMethod.fundingSource
    }

    override fun getInstallmentOptions(
        installmentConfiguration: InstallmentConfiguration?,
        cardType: CardType?,
        isCardTypeReliable: Boolean
    ): List<InstallmentModel> {
        val isDebit = getFundingSource() == DEBIT_FUNDING_SOURCE
        return if (isDebit) {
            emptyList()
        } else {
            InstallmentUtils.makeInstallmentOptions(installmentConfiguration, cardType, isCardTypeReliable)
        }
    }

    override fun getSupportedCardTypes(): List<CardType> = cardConfiguration.supportedCardTypes

    private fun detectCardLocally(cardNumber: String): List<DetectedCardType> {
        Logger.d(TAG, "detectCardLocally")
        if (cardNumber.isEmpty()) {
            return emptyList()
        }
        val supportedCardTypes = cardConfiguration.supportedCardTypes
        val estimateCardTypes = CardType.estimate(cardNumber)
        return estimateCardTypes.map { localDetectedCard(it, supportedCardTypes) }
    }

    private fun localDetectedCard(cardType: CardType, supportedCardTypes: List<CardType>): DetectedCardType {
        return DetectedCardType(
            cardType,
            isReliable = false,
            enableLuhnCheck = true,
            cvcPolicy = when {
                noCvcBrands.contains(cardType) -> Brand.FieldPolicy.HIDDEN
                else -> Brand.FieldPolicy.REQUIRED
            },
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = supportedCardTypes.contains(cardType)
        )
    }
}
