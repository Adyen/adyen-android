/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/11/2025.
 */

@file:Suppress("TooManyFunctions")

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.ExpiryDateParser
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.helper.runCompileOnly
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.state.model.getPaymentDataValue
import com.adyen.checkout.core.components.paymentmethod.CardDetails
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import com.adyen.threeds2.ThreeDS2Service

@Suppress("ReturnCount")
internal fun CardComponentState.toPaymentComponentState(
    componentParams: CardComponentParams,
    cardEncryptor: BaseCardEncryptor,
    genericEncryptor: BaseGenericEncryptor,
    sdkDataProvider: SdkDataProvider,
    onEncryptionFailed: (EncryptionException) -> Unit,
    onPublicKeyNotFound: (InternalCheckoutError) -> Unit,
): CardPaymentComponentState {
    val publicKey = componentParams.publicKey(onPublicKeyNotFound = onPublicKeyNotFound)
        ?: return invalidCardPaymentComponentState()

    val encryptedCard = encryptCard(
        cardEncryptor = cardEncryptor,
        publicKey = publicKey,
        onEncryptionFailed = onEncryptionFailed,
    ) ?: return invalidCardPaymentComponentState()

    val encryptedKcpCardPassword = kcpCardPassword.getPaymentDataValue()?.let { kcpCardPassword ->
        encryptKcpCardPassword(
            genericEncryptor = genericEncryptor,
            publicKey = publicKey,
            kcpCardPassword = kcpCardPassword,
            onEncryptionFailed = onEncryptionFailed,
        ) ?: return invalidCardPaymentComponentState()
    }

    val cardDetails = createCardDetails(
        encryptedCard = encryptedCard,
        holderName = holderName.getPaymentDataValue(),
        cardBrand = cardBrand(),
        sdkDataProvider = sdkDataProvider,
        kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumber.getPaymentDataValue(),
        encryptedKcpCardPassword = encryptedKcpCardPassword,
    )

    val paymentComponentData = createPaymentComponentData(
        cardDetails = cardDetails,
        storePaymentMethod = storePaymentMethod(componentParams),
        socialSecurityNumber = socialSecurityNumber.getPaymentDataValue(),
        componentParams = componentParams,
    )

    return createPaymentComponentState(paymentComponentData)
}

private fun CardComponentState.encryptCard(
    cardEncryptor: BaseCardEncryptor,
    publicKey: String,
    onEncryptionFailed: (EncryptionException) -> Unit,
): EncryptedCard? {
    return try {
        val unencryptedCardBuilder = UnencryptedCard.Builder()
        cardNumber.getPaymentDataValue()?.let {
            unencryptedCardBuilder.setNumber(it)
        }

        securityCode.getPaymentDataValue()?.let {
            unencryptedCardBuilder.setCvc(it)
        }

        expiryDate.getPaymentDataValue()?.let {
            ExpiryDateParser.parseToMonthAndYear(it, returnFullYear = true)
        }?.let { (expiryMonth, expiryYear) ->
            unencryptedCardBuilder.setExpiryDate(
                expiryMonth = expiryMonth,
                expiryYear = expiryYear,
            )
        }

        cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        onEncryptionFailed(e)
        null
    }
}

private fun encryptKcpCardPassword(
    genericEncryptor: BaseGenericEncryptor,
    publicKey: String,
    kcpCardPassword: String,
    onEncryptionFailed: (EncryptionException) -> Unit,
): String? {
    return try {
        genericEncryptor.encryptField(
            fieldKeyToEncrypt = ENCRYPTION_KEY_FOR_KCP_PASSWORD,
            fieldValueToEncrypt = kcpCardPassword,
            publicKey = publicKey,
        )
    } catch (e: EncryptionException) {
        onEncryptionFailed(e)
        null
    }
}

private fun createCardDetails(
    encryptedCard: EncryptedCard,
    sdkDataProvider: SdkDataProvider,
    holderName: String?,
    cardBrand: CardBrand?,
    encryptedKcpCardPassword: String?,
    kcpBirthDateOrTaxNumber: String?,
) = CardDetails(
    type = CardDetails.PAYMENT_METHOD_TYPE,
    sdkData = sdkDataProvider.createEncodedSdkData(
        threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion },
    ),
    encryptedCardNumber = encryptedCard.encryptedCardNumber,
    encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth,
    encryptedExpiryYear = encryptedCard.encryptedExpiryYear,
    encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
    holderName = holderName,
    brand = cardBrand?.txVariant,
    encryptedPassword = encryptedKcpCardPassword,
    taxNumber = kcpBirthDateOrTaxNumber,
)

private fun createPaymentComponentData(
    cardDetails: CardDetails,
    storePaymentMethod: Boolean?,
    socialSecurityNumber: String?,
    componentParams: CardComponentParams,
) = PaymentComponentData(
    paymentMethod = cardDetails,
    storePaymentMethod = storePaymentMethod,
    shopperReference = componentParams.shopperReference,
    order = null,
    amount = componentParams.amount,
    socialSecurityNumber = socialSecurityNumber,
)

private fun createPaymentComponentState(
    paymentComponentData: PaymentComponentData<CardDetails>,
): CardPaymentComponentState {
    return CardPaymentComponentState(
        data = paymentComponentData,
        isValid = true,
    )
}

private fun invalidCardPaymentComponentState() = CardPaymentComponentState(
    data = PaymentComponentData(null, null, null),
    isValid = false,
)

private fun CardComponentState.cardBrand(): CardBrand? {
    val supportedDetectedCardTypes = detectedCardTypes.filter { it.isSupported && it.isReliable }
    return if (supportedDetectedCardTypes.size > 1) {
        selectedCardBrand ?: supportedDetectedCardTypes.firstOrNull()?.cardBrand
    } else {
        supportedDetectedCardTypes.firstOrNull()?.cardBrand
    }
}

private fun CardComponentState.storePaymentMethod(componentParams: CardComponentParams): Boolean? {
    return storePaymentMethod.takeIf { componentParams.showStorePayment }
}

private fun CardComponentParams.publicKey(onPublicKeyNotFound: (InternalCheckoutError) -> Unit): String? {
    return publicKey ?: run {
        onPublicKeyNotFound(GenericError("Public key is missing."))
        null
    }
}

private const val ENCRYPTION_KEY_FOR_KCP_PASSWORD = "password"
