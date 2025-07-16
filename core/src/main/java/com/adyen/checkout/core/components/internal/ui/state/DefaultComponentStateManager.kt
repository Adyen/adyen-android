/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.model.ComponentFieldState
import com.adyen.checkout.core.components.internal.ui.state.model.FieldId
import com.adyen.checkout.core.components.internal.ui.state.model.Validation
import com.adyen.checkout.core.components.internal.ui.state.model.updateFieldState
import com.adyen.checkout.core.components.internal.ui.state.transformer.DefaultTransformerRegistry
import com.adyen.checkout.core.components.internal.ui.state.transformer.FieldTransformerRegistry
import com.adyen.checkout.core.components.internal.ui.state.updater.StateUpdaterRegistry
import com.adyen.checkout.core.components.internal.ui.state.validator.FieldValidatorRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultComponentStateManager<S : ComponentState, FI : FieldId>(
    private val factory: ComponentStateFactory<S, FI>,
    private val validationRegistry: FieldValidatorRegistry<S, FI>,
    private val stateUpdaterRegistry: StateUpdaterRegistry<S, FI>,
    private val transformerRegistry: FieldTransformerRegistry<FI> = DefaultTransformerRegistry(),
) : ComponentStateManager<S, FI> {

    private val _state = MutableStateFlow(factory.createDefaultComponentState())
    override val state: StateFlow<S> = _state.asStateFlow()

    override val isValid: Boolean
        get() {
            validateFields { fieldState -> fieldState.validation == null }
            return _state.value.isValid
        }

    override fun updateState(update: S.() -> S) {
        _state.update(update)

        // Revalidate all validated fields, to make sure they take the new state values
        validateFields { fieldState -> fieldState.validation != null }
    }

    private fun validateFields(validationPredicate: (ComponentFieldState<Any>) -> Boolean) {
        factory.getFieldIds()
            .filter { fieldId ->
                val fieldState = stateUpdaterRegistry.getFieldState<Any>(_state.value, fieldId)
                validationPredicate.invoke(fieldState)
            }
            .forEach { fieldId ->
                val fieldState = stateUpdaterRegistry.getFieldState<Any>(_state.value, fieldId)
                updateField(
                    fieldId = fieldId,
                    value = fieldState.value, // Ensure the current value is validated,
                )
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
                _state.value,
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
