/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

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
import com.adyen.threeds2.ThreeDS2Service

@Suppress("ReturnCount", "LongParameterList")
internal fun StoredCardComponentState.toPaymentComponentState(
    cardEncryptor: BaseCardEncryptor,
    sdkDataProvider: SdkDataProvider,
    storedPaymentMethodId: String?,
    paymentMethodType: String,
    onEncryptionFailed: (EncryptionException) -> Unit,
    onPublicKeyNotFound: (InternalCheckoutError) -> Unit,
    publicKey: String?,
): CardPaymentComponentState {
    publicKey ?: run {
        onPublicKeyNotFound(GenericError("Public key is missing."))
        return invalidCardPaymentComponentState()
    }

    val encryptedCard = encryptCard(
        cardEncryptor = cardEncryptor,
        publicKey = publicKey,
        onEncryptionFailed = onEncryptionFailed,
    ) ?: return invalidCardPaymentComponentState()

    val cardDetails = createCardDetails(
        storedCardId = storedPaymentMethodId,
        encryptedCard = encryptedCard,
        sdkDataProvider = sdkDataProvider,
        paymentMethodType = paymentMethodType,
    )

    val paymentComponentData = createPaymentComponentData(cardDetails)

    return createPaymentComponentState(paymentComponentData)
}

@Suppress("LongParameterList")
private fun StoredCardComponentState.encryptCard(
    cardEncryptor: BaseCardEncryptor,
    publicKey: String,
    onEncryptionFailed: (EncryptionException) -> Unit,
): EncryptedCard? {
    return try {
        val unencryptedCardBuilder = UnencryptedCard.Builder()
        securityCode.getPaymentDataValue()?.let {
            unencryptedCardBuilder.setCvc(it)
        }
        cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        onEncryptionFailed(e)
        null
    }
}

private fun invalidCardPaymentComponentState() = CardPaymentComponentState(
    data = PaymentComponentData(null, null, null),
    isValid = false,
)

private fun createPaymentComponentData(
    cardDetails: CardDetails,
) = PaymentComponentData(
    paymentMethod = cardDetails,
    storePaymentMethod = null,
    order = null,
)

private fun createPaymentComponentState(
    paymentComponentData: PaymentComponentData<CardDetails>,
): CardPaymentComponentState {
    return CardPaymentComponentState(
        data = paymentComponentData,
        isValid = true,
    )
}

private fun createCardDetails(
    storedCardId: String?,
    encryptedCard: EncryptedCard,
    sdkDataProvider: SdkDataProvider,
    paymentMethodType: String,
) = CardDetails(
    type = paymentMethodType,
    sdkData = sdkDataProvider.createEncodedSdkData(
        threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }.getOrNull(),
    ),
    storedPaymentMethodId = storedPaymentMethodId(storedCardId),
    encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
)

private fun storedPaymentMethodId(storedPaymentMethodId: String?): String {
    return storedPaymentMethodId ?: "ID_NOT_FOUND"
}
