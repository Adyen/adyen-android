/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/2/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.state

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.transformer.DefaultTransformerRegistry
import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry
import com.adyen.checkout.components.core.internal.ui.model.updateFieldState
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultDelegateStateManager<S : DelegateState, FI>(
    private val factory: DelegateStateFactory<S, FI>,
    private val validationRegistry: FieldValidatorRegistry<FI>,
    private val stateUpdaterRegistry: StateUpdaterRegistry<S, FI>,
    private val transformerRegistry: FieldTransformerRegistry<FI> = DefaultTransformerRegistry(),
) : DelegateStateManager<S, FI> {

    private val _state = MutableStateFlow(factory.createDefaultDelegateState())
    override val state: StateFlow<S> = _state.asStateFlow()

    override val isValid
        get() = run {
            validateNonValidatedFields()
            _state.value.isValid
        }

    private fun validateNonValidatedFields() {
        factory.getFieldIds().forEach { fieldId ->
            val fieldState = stateUpdaterRegistry.getFieldState<Any>(_state.value, fieldId)

            if (fieldState.validation == null) {
                updateField(
                    fieldId = fieldId,
                    value = fieldState.value, // Ensure the current value is validated,
                    hasFocus = fieldState.hasFocus,
                )
            }
        }
    }

    override fun <T> updateFieldValue(fieldId: FI, value: T?) = updateField(fieldId, value = value)

    override fun updateFieldFocus(fieldId: FI, hasFocus: Boolean) =
        updateField<Unit>(fieldId, hasFocus = hasFocus)

    override fun highlightAllFieldValidationErrors() {
        // Flag to focus only the first invalid field
        var isErrorFieldFocused = false

        factory.getFieldIds().forEach { fieldId ->
            val fieldState = stateUpdaterRegistry.getFieldState<Any>(_state.value, fieldId)

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

    private fun <T> updateField(
        fieldId: FI,
        value: T? = null,
        hasFocus: Boolean? = null,
        shouldHighlightValidationError: Boolean? = null,
    ) {
        val validation = value?.let {
            validationRegistry.validate(
                fieldId,
                transformerRegistry.transform(fieldId, it),
            )
        }

        val fieldState = stateUpdaterRegistry.getFieldState<T>(_state.value, fieldId)
        val updatedFieldState = fieldState.updateFieldState(
            value = value,
            validation = validation,
            hasFocus = hasFocus,
            shouldHighlightValidationError = shouldHighlightValidationError,
        )

        _state.update {
            stateUpdaterRegistry.updateFieldState(_state.value, fieldId, updatedFieldState)
        }
    }
}
