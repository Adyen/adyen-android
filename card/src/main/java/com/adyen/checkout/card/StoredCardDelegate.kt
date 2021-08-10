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
import com.adyen.checkout.card.repository.PublicKeyRepository
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class StoredCardDelegate(
    private val storedPaymentMethod: StoredPaymentMethod,
    cardConfiguration: CardConfiguration,
    publicKeyRepository: PublicKeyRepository
) : CardDelegate(cardConfiguration, publicKeyRepository) {

    private val cardType = CardType.getByBrandName(storedPaymentMethod.brand.orEmpty())
    private val storedDetectedCardTypes = if (cardType != null) {
        listOf(
            DetectedCardType(
                cardType,
                isReliable = true,
                enableLuhnCheck = true,
                cvcPolicy = when {
                    cardConfiguration.isHideCvcStoredCard || noCvcBrands.contains(cardType) -> Brand.FieldPolicy.HIDDEN
                    else -> Brand.FieldPolicy.REQUIRED
                },
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED
            )
        )
    } else {
        emptyList()
    }

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun validateCardNumber(cardNumber: String): FieldState<String> {
        return FieldState(
            cardNumber,
            Validation.Valid
        )
    }

    override fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        return FieldState(
            expiryDate,
            Validation.Valid
        )
    }

    override fun validateSecurityCode(securityCode: String, cardType: DetectedCardType?): FieldState<String> {
        return if (cardConfiguration.isHideCvcStoredCard || noCvcBrands.contains(cardType?.cardType)) {
            FieldState(
                securityCode,
                Validation.Valid
            )
        } else {
            CardValidationUtils.validateSecurityCode(securityCode, cardType)
        }
    }

    override fun validateHolderName(holderName: String): FieldState<String> {
        return FieldState(
            holderName,
            Validation.Valid
        )
    }

    override fun validateSocialSecurityNumber(socialSecurityNumber: String): FieldState<String> {
        return FieldState(socialSecurityNumber, Validation.Valid)
    }

    override fun isCvcHidden(): Boolean {
        return cardConfiguration.isHideCvcStoredCard || noCvcBrands.contains(cardType)
    }

    override fun isSocialSecurityNumberRequired(): Boolean {
        return false
    }

    override fun requiresInput(): Boolean {
        return !cardConfiguration.isHideCvcStoredCard
    }

    override fun isHolderNameRequired(): Boolean {
        return false
    }

    override fun detectCardType(
        cardNumber: String,
        publicKey: String?,
        coroutineScope: CoroutineScope
    ): List<DetectedCardType> {
        return storedDetectedCardTypes
    }

    fun getStoredCardInputData(): CardInputData {
        val storedCardInputData = CardInputData()
        storedCardInputData.cardNumber = storedPaymentMethod.lastFour.orEmpty()

        try {
            val storedDate = ExpiryDate(storedPaymentMethod.expiryMonth.orEmpty().toInt(), storedPaymentMethod.expiryYear.orEmpty().toInt())
            storedCardInputData.expiryDate = storedDate
        } catch (e: NumberFormatException) {
            Logger.e(TAG, "Failed to parse stored Date", e)
            storedCardInputData.expiryDate = ExpiryDate.EMPTY_DATE
        }

        return storedCardInputData
    }

    fun getId(): String {
        return storedPaymentMethod.id ?: "ID_NOT_FOUND"
    }
}
