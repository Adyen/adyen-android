/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/2/2021.
 */
package com.adyen.checkout.card

import com.adyen.checkout.card.internal.ui.model.CardDelegateState
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.util.DetectedCardTypesUtils
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.internal.util.runCompileOnly
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.channels.Channel

/**
 * Represents the state of [CardComponent].
 */
data class CardComponentState(
    override val data: PaymentComponentData<CardPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
    val cardBrand: CardBrand?,
    val binValue: String,
    val lastFourDigits: String?,
) : PaymentComponentState<CardPaymentMethod>

private const val BIN_VALUE_LENGTH = 6
private const val BIN_VALUE_EXTENDED_LENGTH = 8
private const val EXTENDED_CARD_NUMBER_LENGTH = 16
private const val LAST_FOUR_LENGTH = 4
private const val ENCRYPTION_KEY_FOR_KCP_PASSWORD = "password"

internal fun CardDelegateState.toComponentState(
    paymentMethod: PaymentMethod,
    order: OrderRequest?,
    analyticsManager: AnalyticsManager,
    cardEncryptor: BaseCardEncryptor,
    genericEncryptor: BaseGenericEncryptor,
    exceptionChannel: Channel<CheckoutException>,
    onBinValueListener: ((binValue: String) -> Unit)?,
): CardComponentState {
    val cardNumber = cardNumberDelegateState.value

    val firstCardBrand = DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(
        detectedCardTypes = detectedCardTypes,
    )?.cardBrand

    val binValue =
        if (cardNumberDelegateState.validation?.isValid() == true && cardNumber.length >= EXTENDED_CARD_NUMBER_LENGTH) {
            cardNumber.take(BIN_VALUE_EXTENDED_LENGTH)
        } else {
            cardNumber.take(BIN_VALUE_LENGTH)
        }

//    @Suppress("UNNECESSARY_SAFE_CALL")
//    if (_componentStateFlow?.value?.binValue != binValue) {
    onBinValueListener?.invoke(binValue)
//    }

    // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
    if (!isValid || publicKey == null) {
        return CardComponentState(
            data = PaymentComponentData(null, null, null),
            isInputValid = false,
            isReady = publicKey != null,
            cardBrand = firstCardBrand,
            binValue = binValue,
            lastFourDigits = null,
        )
    }

    val unencryptedCardBuilder = UnencryptedCard.Builder()

    val encryptedCard: EncryptedCard = try {
        unencryptedCardBuilder.setNumber(cardNumberDelegateState.value)
        if (!isCvcHidden()) {
            val cvc = cardSecurityCodeDelegateState.value
            if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
        }
        val expiryDateResult = cardExpiryDateDelegateState.value
        if (expiryDateResult != EMPTY_DATE) {
            unencryptedCardBuilder.setExpiryDate(
                expiryMonth = expiryDateResult.expiryMonth.toString(),
                expiryYear = expiryDateResult.expiryYear.toString(),
            )
        }

        cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        val event = GenericEvents.error(
            paymentMethod.type.orEmpty(),
            ErrorEvent.ENCRYPTION,
        )
        analyticsManager.trackEvent(event)

        exceptionChannel.trySend(e)

        return CardComponentState(
            data = PaymentComponentData(null, null, null),
            isInputValid = false,
            isReady = true,
            cardBrand = firstCardBrand,
            binValue = binValue,
            lastFourDigits = null,
        )
    }

    return mapComponentState(
        paymentMethod,
        order,
        analyticsManager,
        genericEncryptor,
        publicKey,
        encryptedCard,
        cardNumber,
        firstCardBrand,
        binValue,
    )
}

private fun CardDelegateState.isCvcHidden(): Boolean {
    return cvcUIState == InputFieldUIState.HIDDEN
}

private fun CardDelegateState.mapComponentState(
    paymentMethod: PaymentMethod,
    order: OrderRequest?,
    analyticsManager: AnalyticsManager,
    genericEncryptor: BaseGenericEncryptor,
    publicKey: String?,
    encryptedCard: EncryptedCard,
    cardNumber: String,
    firstCardBrand: CardBrand?,
    binValue: String
): CardComponentState {
    val cardPaymentMethod = CardPaymentMethod(
        type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
        checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
    ).apply {
        encryptedCardNumber = encryptedCard.encryptedCardNumber
        encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth
        encryptedExpiryYear = encryptedCard.encryptedExpiryYear

        if (!isCvcHidden()) {
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode
        }

        if (isHolderNameRequired()) {
            holderName = cardHolderNameDelegateState.value
        }

        if (isKCPAuthRequired()) {
            publicKey?.let { publicKey ->
                encryptedPassword = genericEncryptor.encryptField(
                    ENCRYPTION_KEY_FOR_KCP_PASSWORD,
                    kcpCardPasswordDelegateState.value,
                    publicKey,
                )
            } ?: throw CheckoutException("Encryption failed because public key cannot be found.")
            taxNumber = kcpBirthDateOrTaxNumberDelegateState.value
        }

        brand = if (isDualBranded) {
            DetectedCardTypesUtils.getSelectedCardType(detectedCardTypes)
        } else {
            val reliableCardBrand = detectedCardTypes.firstOrNull { it.isReliable }
            val firstDetectedBrand = detectedCardTypes.firstOrNull()
            reliableCardBrand ?: firstDetectedBrand
        }?.cardBrand?.txVariant

        fundingSource = paymentMethod.fundingSource

        threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }
    }

    val paymentComponentData = makePaymentComponentData(cardPaymentMethod, order)

    val lastFour = cardNumber.takeLast(LAST_FOUR_LENGTH)

    return CardComponentState(
        data = paymentComponentData,
        isInputValid = true,
        isReady = true,
        cardBrand = firstCardBrand,
        binValue = binValue,
        lastFourDigits = lastFour,
    )
}

private fun CardDelegateState.isHolderNameRequired(): Boolean {
    return componentParams.isHolderNameRequired
}

private fun CardDelegateState.isKCPAuthRequired(): Boolean {
    return componentParams.kcpAuthVisibility == KCPAuthVisibility.SHOW
}

private fun CardDelegateState.makePaymentComponentData(
    cardPaymentMethod: CardPaymentMethod,
    order: OrderRequest?,
): PaymentComponentData<CardPaymentMethod> {
    return PaymentComponentData(
        paymentMethod = cardPaymentMethod,
        storePaymentMethod = if (showStorePaymentField) storedPaymentMethodSwitchDelegateState.value else null,
        shopperReference = componentParams.shopperReference,
        order = order,
        amount = componentParams.amount,
    ).apply {
        if (isSocialSecurityNumberRequired()) {
            socialSecurityNumber = socialSecurityNumberDelegateState.value
        }
        if (isAddressRequired()) {
            billingAddress = AddressFormUtils.makeAddressData(
                addressOutputData = addressState,
                addressFormUIState = addressFormUIState,
            )
        }
        if (isInstallmentsRequired()) {
            installments = InstallmentUtils.makeInstallmentModelObject(installmentOptionDelegateState.value)
        }
    }
}

private fun CardDelegateState.isSocialSecurityNumberRequired(): Boolean {
    return componentParams.socialSecurityNumberVisibility == SocialSecurityNumberVisibility.SHOW
}

private fun CardDelegateState.isAddressRequired(): Boolean {
    return AddressFormUtils.isAddressRequired(addressFormUIState)
}

private fun CardDelegateState.isInstallmentsRequired(): Boolean {
    return installmentOptions.isNotEmpty()
}
