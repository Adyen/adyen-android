/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.util.CountryUtils
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.components.core.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.R
import com.adyen.checkout.mbway.internal.ui.model.FocussedView
import com.adyen.checkout.mbway.internal.ui.model.InputError
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DefaultMBWayDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
    private val analyticsRepository: AnalyticsRepository,
    private val submitHandler: SubmitHandler<MBWayComponentState>,
) : MBWayDelegate {

    private val _viewStateFlow = MutableStateFlow(getInitialViewState())
    override val viewStateFlow: Flow<MBWayViewState> = _viewStateFlow

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<MBWayComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(MbWayComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<MBWayComponentState> = submitHandler.submitFlow

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        setupAnalytics(coroutineScope)

        viewStateFlow
            .onEach { updateComponentState() }
            .launchIn(coroutineScope)
    }

    private fun setupAnalytics(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "setupAnalytics")
        coroutineScope.launch {
            analyticsRepository.setupAnalytics()
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<MBWayComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    private fun getInitialViewState(): MBWayViewState {
        val countries = getCountryInfoList()
        return MBWayViewState(
            phoneNumber = "",
            phoneNumberError = null,
            countries = countries,
            selectedCountry = countries.first(),
            focussedView = FocussedView.PHONE_NUMBER,
        )
    }

    private fun getCountryInfoList(): List<CountryModel> =
        CountryUtils.getCountries(SUPPORTED_COUNTRIES).map {
            with(it) {
                CountryModel(
                    isoCode = isoCode,
                    callingCode = callingCode,
                    countryName = CountryUtils.getCountryName(isoCode, componentParams.shopperLocale),
                    emoji = emoji,
                )
            }
        }

    override fun onCountrySelected(country: CountryModel) {
        Logger.v(TAG, "onCountryCodeSelected")
        val currentViewState = _viewStateFlow.value

        val newViewState = currentViewState.copy(
            selectedCountry = country,
        )

        _viewStateFlow.tryEmit(newViewState)
    }

    override fun onPhoneNumberChanged(phoneNumber: String) {
        Logger.v(TAG, "onPhoneNumberChanged")
        val sanitizedNumber = phoneNumber
            .trim()
            .trimStart('0')

        val currentViewState = _viewStateFlow.value

        val newViewState = currentViewState.copy(
            phoneNumber = sanitizedNumber,
            phoneNumberError = null,
        )

        _viewStateFlow.tryEmit(newViewState)
    }

    override fun onViewFocussed(focussedView: FocussedView) {
        val currentViewState = _viewStateFlow.value

        var phoneNumberError: InputError? = null
        // Validate previously focussed view
        when (currentViewState.focussedView) {
            FocussedView.PHONE_NUMBER -> {
                phoneNumberError = validatePhoneNumberInput(
                    fullPhoneNumber = currentViewState.selectedCountry.callingCode + currentViewState.phoneNumber,
                    requestFocus = false,
                )
            }

            FocussedView.TEST -> Unit
        }

        // Remove validation from newly focussed view
        when (focussedView) {
            FocussedView.PHONE_NUMBER -> {
                phoneNumberError = null
            }

            FocussedView.TEST -> Unit
        }

        val newViewState = currentViewState.copy(
            phoneNumberError = phoneNumberError,
            focussedView = focussedView,
        )

        _viewStateFlow.tryEmit(newViewState)
    }

    @VisibleForTesting
    internal fun updateComponentState() {
        val componentState = createComponentState()
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(): MBWayComponentState {
        val viewState = _viewStateFlow.value
        val selectedCallingCode = viewState.selectedCountry.callingCode
        val fullPhoneNumber = selectedCallingCode + viewState.phoneNumber

        val paymentMethod = MBWayPaymentMethod(
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsRepository.getCheckoutAttemptId(),
            telephoneNumber = fullPhoneNumber,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        val isInputValid = ValidationUtils.isPhoneNumberValid(fullPhoneNumber)

        return MBWayComponentState(
            data = paymentComponentData,
            isInputValid = isInputValid,
            isReady = true,
        )
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(state)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    // TODO: Check if we should call this directly instead of relying on the view?
    override fun highlightValidationErrors() {
        val currentViewState = _viewStateFlow.value

        val phoneNumberError = validatePhoneNumberInput(
            fullPhoneNumber = currentViewState.selectedCountry.callingCode + currentViewState.phoneNumber,
            // TODO: See how we can keep the error visible while requesting focus
            requestFocus = true,
        )

        val newViewState = currentViewState.copy(
            phoneNumberError = phoneNumberError,
        )

        _viewStateFlow.tryEmit(newViewState)
    }

    private fun validatePhoneNumberInput(
        fullPhoneNumber: String,
        requestFocus: Boolean,
    ): InputError? {
        return if (ValidationUtils.isPhoneNumberValid(fullPhoneNumber)) {
            null
        } else {
            InputError(
                messageRes = R.string.checkout_mbway_phone_number_not_valid,
                requestFocus = requestFocus,
            )
        }
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"

        private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
