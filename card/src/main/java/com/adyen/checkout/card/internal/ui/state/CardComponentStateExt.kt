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
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.InstallmentPlan
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.helper.runCompileOnly
import com.adyen.checkout.core.components.data.Address
import com.adyen.checkout.core.components.data.Installments
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

@Suppress("ReturnCount", "LongParameterList")
internal fun CardComponentState.toPaymentComponentState(
    publicKey: String?,
    componentParams: CardComponentParams,
    cardEncryptor: BaseCardEncryptor,
    genericEncryptor: BaseGenericEncryptor,
    sdkDataProvider: SdkDataProvider,
    paymentMethodType: String,
    onEncryptionFailed: (EncryptionException) -> Unit,
    onPublicKeyNotFound: (InternalCheckoutError) -> Unit,
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
        paymentMethodType = paymentMethodType,
        kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumber.getPaymentDataValue(),
        encryptedKcpCardPassword = encryptedKcpCardPassword,
    )

    val paymentComponentData = createPaymentComponentData(
        cardDetails = cardDetails,
        storePaymentMethod = storePaymentMethod(componentParams),
        socialSecurityNumber = socialSecurityNumber.getPaymentDataValue(),
        billingAddress = getBillingAddress(),
        installments = getInstallments(),
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

@Suppress("LongParameterList")
private fun createCardDetails(
    encryptedCard: EncryptedCard,
    sdkDataProvider: SdkDataProvider,
    holderName: String?,
    cardBrand: CardBrand?,
    paymentMethodType: String,
    encryptedKcpCardPassword: String?,
    kcpBirthDateOrTaxNumber: String?,
) = CardDetails(
    type = paymentMethodType,
    sdkData = sdkDataProvider.createEncodedSdkData(
        threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }.getOrNull(),
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

private fun CardComponentState.getBillingAddress(): Address? {
    postalCode.getPaymentDataValue()?.let {
        return Address(
            postalCode = it,
        )
    }

    return null
}

private fun createPaymentComponentData(
    cardDetails: CardDetails,
    storePaymentMethod: Boolean?,
    socialSecurityNumber: String?,
    billingAddress: Address?,
    installments: Installments?,
) = PaymentComponentData(
    paymentMethod = cardDetails,
    storePaymentMethod = storePaymentMethod,
    billingAddress = billingAddress,
    order = null,
    socialSecurityNumber = socialSecurityNumber,
    installments = installments,
)

private fun CardComponentState.getInstallments(): Installments? {
    val selectedInstallment = installmentState.selectedInstallment ?: return null

    return when (selectedInstallment) {
        InstallmentModel.OneTime -> null
        InstallmentModel.Revolving -> Installments(
            plan = InstallmentPlan.REVOLVING.type,
            value = 1, // The number of installments for revolving is always 1
        )
        is InstallmentModel.Regular ->
            Installments(
                plan = InstallmentPlan.REGULAR.type,
                value = selectedInstallment.numberOfInstallments,
            )
    }
}

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

/**
 * We only prefill the card brand in the onSubmit payload if we are sure the card has a single brand identified on the
 * backend (reliable) or if the shopper manually selected that brand. In all other cases we cannot reliably assume
 * there is only one possible brand for the /payments call
 */
private fun CardComponentState.cardBrand(): CardBrand? {
    val selectedOrReliableCardBrandData = when (cardBrandState) {
        is CardBrandState.NoBrandsDetected,
        is CardBrandState.UnsupportedBrand,
        is CardBrandState.HiddenBrand,
        is CardBrandState.SingleUnreliableBrand,
        is CardBrandState.SingleReliableWithHiddenBrand,
        is CardBrandState.DualBrand -> null

        is CardBrandState.SingleReliableBrand -> cardBrandState.cardBrandData.cardBrand
        is CardBrandState.DualBrandWithShopperSelection -> cardBrandState.shopperSelectedCardBrandData.cardBrand
    }
    return selectedOrReliableCardBrandData
}

private fun CardComponentState.storePaymentMethod(componentParams: CardComponentParams): Boolean? {
    return storePaymentMethod.takeIf { componentParams.showStorePaymentMethod }
}

private const val ENCRYPTION_KEY_FOR_KCP_PASSWORD = "password"
