/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/11/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.exception.ComponentError
import com.adyen.checkout.core.common.helper.runCompileOnly
import com.adyen.checkout.core.common.ui.model.ExpiryDate
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.threeds2.ThreeDS2Service

@Suppress("ReturnCount")
internal fun CardComponentState.toPaymentComponentState(
    componentParams: CardComponentParams,
    cardEncryptor: BaseCardEncryptor,
    sdkDataProvider: SdkDataProvider,
    onEncryptionFailed: (EncryptionException) -> Unit,
    onPublicKeyNotFound: (ComponentError) -> Unit,
): CardPaymentComponentState {
    val publicKey = componentParams.publicKey(onPublicKeyNotFound = onPublicKeyNotFound)
        ?: return invalidCardPaymentComponentState()

    val encryptedCard = encryptCard(
        cardEncryptor = cardEncryptor,
        publicKey = publicKey,
        onEncryptionFailed = onEncryptionFailed,
    ) ?: return invalidCardPaymentComponentState()

    val cardPaymentMethod = createPaymentMethod(
        encryptedCard = encryptedCard,
        holderName = holderName(componentParams),
        cardBrand = cardBrand(),
        sdkDataProvider = sdkDataProvider,
    )

    val paymentComponentData = createPaymentComponentData(
        cardPaymentMethod = cardPaymentMethod,
        storePaymentMethod = storePaymentMethod(componentParams),
        componentParams = componentParams,
    )

    return createPaymentComponentState(paymentComponentData)
}

@Suppress("LongParameterList")
private fun CardComponentState.encryptCard(
    cardEncryptor: BaseCardEncryptor,
    publicKey: String,
    onEncryptionFailed: (EncryptionException) -> Unit,
): EncryptedCard? {
    val unencryptedCardBuilder = UnencryptedCard.Builder()
    return try {
        unencryptedCardBuilder.setNumber(cardNumber.text)
        // TODO - Card. Add isCvcHidden check
        val cvc = securityCode.text
        if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
        if (expiryDate.text.isNotBlank()) {
            val expiryDate = ExpiryDate.from(expiryDate.text)
            unencryptedCardBuilder.setExpiryDate(
                expiryMonth = expiryDate.expiryMonth.toString(),
                expiryYear = expiryDate.expiryYear.toString(),
            )
        }

        cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        onEncryptionFailed(e)
        null
    }
}

private fun createPaymentMethod(
    encryptedCard: EncryptedCard,
    sdkDataProvider: SdkDataProvider,
    holderName: String?,
    cardBrand: CardBrand?,
) = CardPaymentMethod(
    type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
    sdkData = sdkDataProvider.createEncodedSdkData(
        threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion },
    ),
    encryptedCardNumber = encryptedCard.encryptedCardNumber,
    encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth,
    encryptedExpiryYear = encryptedCard.encryptedExpiryYear,
    // TODO - Card. Add isCvcHidden check
    encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
    holderName = holderName,
    brand = cardBrand?.txVariant,
)

private fun createPaymentComponentData(
    cardPaymentMethod: CardPaymentMethod,
    storePaymentMethod: Boolean?,
    componentParams: CardComponentParams,
) = PaymentComponentData(
    paymentMethod = cardPaymentMethod,
    storePaymentMethod = storePaymentMethod,
    shopperReference = componentParams.shopperReference,
    order = null,
    amount = componentParams.amount,
)

private fun createPaymentComponentState(
    paymentComponentData: PaymentComponentData<CardPaymentMethod>,
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

private fun CardComponentState.holderName(componentParams: CardComponentParams) =
    if (componentParams.isHolderNameRequired && holderName.text.isNotBlank()) {
        holderName.text
    } else {
        null
    }

private fun CardComponentState.storePaymentMethod(componentParams: CardComponentParams) =
    if (componentParams.isStorePaymentFieldVisible) {
        storePaymentMethod
    } else {
        null
    }

private fun CardComponentParams.publicKey(onPublicKeyNotFound: (ComponentError) -> Unit): String? {
    return publicKey ?: run {
        onPublicKeyNotFound(ComponentError("Public key is missing."))
        null
    }
}
