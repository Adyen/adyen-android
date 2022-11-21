/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2022.
 */

package com.adyen.checkout.bcmc

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.card.CardValidationMapper
import com.adyen.checkout.card.R
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.util.CardValidationUtils
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.exception.EncryptionException
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions")
internal class DefaultBcmcDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val publicKeyRepository: PublicKeyRepository,
    override val componentParams: BcmcComponentParams,
    private val cardValidationMapper: CardValidationMapper,
    private val cardEncrypter: CardEncrypter,
) : BcmcDelegate {

    private val inputData = BcmcInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<BcmcOutputData> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PaymentComponentState<CardPaymentMethod>> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val outputData get() = _outputDataFlow.value

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(BcmcComponentViewType)

    private var publicKey: String? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        fetchPublicKey(coroutineScope)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PaymentComponentState<CardPaymentMethod>>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = exceptionFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    private fun fetchPublicKey(coroutineScope: CoroutineScope) {
        Logger.d(TAG, "fetchPublicKey")
        coroutineScope.launch {
            publicKeyRepository.fetchPublicKey(
                environment = componentParams.environment,
                clientKey = componentParams.clientKey
            ).fold(
                onSuccess = { key ->
                    Logger.d(TAG, "Public key fetched")
                    publicKey = key
                    updateComponentState(outputData)
                },
                onFailure = { e ->
                    Logger.e(TAG, "Unable to fetch public key")
                    exceptionChannel.trySend(ComponentException("Unable to fetch publicKey.", e))
                }
            )
        }
    }

    override fun updateInputData(update: BcmcInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()

        _outputDataFlow.tryEmit(outputData)

        updateComponentState(outputData)
    }

    private fun createOutputData() = BcmcOutputData(
        cardNumberField = validateCardNumber(inputData.cardNumber),
        expiryDateField = CardValidationUtils.validateExpiryDate(inputData.expiryDate, Brand.FieldPolicy.REQUIRED),
        cardHolderNameField = validateHolderName(inputData.cardHolderName),
        isStoredPaymentMethodEnabled = inputData.isStorePaymentSelected
    )

    private fun validateCardNumber(cardNumber: String): FieldState<String> {
        val validation =
            CardValidationUtils.validateCardNumber(cardNumber, enableLuhnCheck = true, isBrandSupported = true)
        return cardValidationMapper.mapCardNumberValidation(cardNumber, validation)
    }

    private fun validateHolderName(holderName: String): FieldState<String> {
        return if (componentParams.isHolderNameRequired && holderName.isBlank()) {
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

    @VisibleForTesting
    internal fun updateComponentState(outputData: BcmcOutputData) {
        Logger.v(TAG, "updateComponentState")
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    @Suppress("ReturnCount")
    private fun createComponentState(
        outputData: BcmcOutputData = this.outputData
    ): PaymentComponentState<CardPaymentMethod> {
        val paymentComponentData = PaymentComponentData<CardPaymentMethod>()

        val publicKey = publicKey

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid || publicKey == null) {
            return PaymentComponentState(
                data = PaymentComponentData(),
                isInputValid = outputData.isValid,
                isReady = publicKey != null,
            )
        }

        val encryptedCard = encryptCardData(outputData, publicKey) ?: return PaymentComponentState(
            data = PaymentComponentData(),
            isInputValid = false,
            isReady = true,
        )

        // BCMC payment method is scheme type.
        val cardPaymentMethod = CardPaymentMethod(
            type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
            encryptedCardNumber = encryptedCard.encryptedCardNumber,
            encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth,
            encryptedExpiryYear = encryptedCard.encryptedExpiryYear,
            threeDS2SdkVersion = get3DS2SdkVersion(),
        ).apply {
            if (componentParams.isHolderNameRequired) {
                holderName = outputData.cardHolderNameField.value
            }
        }
        paymentComponentData.apply {
            paymentMethod = cardPaymentMethod
            storePaymentMethod = outputData.isStoredPaymentMethodEnabled
            shopperReference = componentParams.shopperReference
        }

        return PaymentComponentState(paymentComponentData, isInputValid = true, isReady = true)
    }

    private fun encryptCardData(
        outputData: BcmcOutputData,
        publicKey: String,
    ): EncryptedCard? = try {
        val unencryptedCardBuilder = UnencryptedCard.Builder()
            .setNumber(outputData.cardNumberField.value)

        val expiryDateResult = outputData.expiryDateField.value
        if (expiryDateResult != ExpiryDate.EMPTY_DATE) {
            unencryptedCardBuilder.setExpiryMonth(expiryDateResult.expiryMonth.toString())
            unencryptedCardBuilder.setExpiryYear(expiryDateResult.expiryYear.toString())
        }

        cardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        exceptionChannel.trySend(e)
        null
    }

    private fun get3DS2SdkVersion(): String? = try {
        ThreeDS2Service.INSTANCE.sdkVersion
    } catch (e: ClassNotFoundException) {
        Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
        null
    } catch (e: NoClassDefFoundError) {
        Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
        null
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun isCardNumberSupported(cardNumber: String?): Boolean {
        if (cardNumber.isNullOrEmpty()) return false
        return CardType.estimate(cardNumber).contains(BcmcComponent.SUPPORTED_CARD_TYPE)
    }

    override fun onCleared() {
        removeObserver()
    }

    override fun getViewProvider(): ViewProvider = BcmcViewProvider

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
