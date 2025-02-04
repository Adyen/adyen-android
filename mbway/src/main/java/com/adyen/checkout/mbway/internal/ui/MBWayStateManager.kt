/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.field.StateManager
import com.adyen.checkout.components.core.internal.ui.model.field.StateUpdaterRegistry
import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry
import com.adyen.checkout.components.core.internal.ui.model.updateFieldState
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.util.CountryUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class MBWayStateManager(
    componentParams: ButtonComponentParams,
    private val transformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    private val validationRegistry: FieldValidatorRegistry<MBWayFieldId>,
    private val stateUpdaterRegistry: StateUpdaterRegistry<MBWayFieldId, MBWayDelegateState>,
) : StateManager<MBWayDelegateState, MBWayFieldId> {

    private val _state = MutableStateFlow(
        MBWayDelegateState(
            countries = getSupportedCountries(componentParams),
            countryCodeFieldState = ComponentFieldDelegateState(getInitiallySelectedCountry(componentParams)),
        ),
    )
    override val state: StateFlow<MBWayDelegateState> = _state

    override val isValid
        get() = run {
            validateNonValidatedFields()
            _state.value.isValid
        }

    private fun getSupportedCountries(componentParams: ComponentParams): List<CountryModel> =
        CountryUtils.getLocalizedCountries(componentParams.shopperLocale, SUPPORTED_COUNTRIES)

    private fun getInitiallySelectedCountry(componentParams: ComponentParams): CountryModel {
        val countries = getSupportedCountries(componentParams)
        return countries.firstOrNull { it.isoCode == ISO_CODE_PORTUGAL } ?: countries.firstOrNull()
        ?: throw IllegalArgumentException("Countries list can not be null")
    }

    private fun validateNonValidatedFields() {
        MBWayFieldId.entries.forEach { fieldId ->
            val fieldState = stateUpdaterRegistry.getFieldState<Any>(fieldId, _state.value)

            if (fieldState.validation == null) {
                updateField(
                    fieldId = fieldId,
                    value = fieldState.value, // Ensure the current value is validated
                )
            }
        }
    }

    override fun <T> updateField(
        fieldId: MBWayFieldId,
        value: T?,
        hasFocus: Boolean?,
        shouldHighlightValidationError: Boolean?,
    ) {
        val validation = value?.let {
            validationRegistry.validate(
                fieldId,
                transformerRegistry.transform(fieldId, value),
            )
        }

        val fieldState = stateUpdaterRegistry.getFieldState<T>(fieldId, _state.value)
        val updatedFieldState = fieldState.updateFieldState(
            value = value,
            validation = validation,
            hasFocus = hasFocus,
            shouldHighlightValidationError = shouldHighlightValidationError,
        )

        _state.update {
            stateUpdaterRegistry.updateFieldState(fieldId, _state.value, updatedFieldState)
        }
    }

    override fun highlightAllFieldValidationErrors() {
        // Flag to focus only the first invalid field
        var isErrorFieldFocused = false

        MBWayFieldId.entries.forEach { fieldId ->
            val fieldState = stateUpdaterRegistry.getFieldState<Any>(fieldId, _state.value)

            val shouldFocus = !isErrorFieldFocused && fieldState.validation is Validation.Invalid
            if (shouldFocus) {
                isErrorFieldFocused = true
            }

            updateField(
                fieldId = fieldId,
                value = fieldState.value, // Ensure the current value is validated
                hasFocus = shouldFocus,
                shouldHighlightValidationError = true,
            )
        }
    }

    companion object {
        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"

        private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
