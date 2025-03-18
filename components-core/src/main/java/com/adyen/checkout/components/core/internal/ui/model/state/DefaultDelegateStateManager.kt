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
import com.adyen.checkout.components.core.internal.ui.model.validation.DefaultStateDependencyRegistry
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.components.core.internal.ui.model.validation.StateDependencyRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.reflect.KProperty1

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultDelegateStateManager<S : DelegateState, FI>(
    private val factory: DelegateStateFactory<S, FI>,
    private val validationRegistry: FieldValidatorRegistry<FI, S>,
    private val stateUpdaterRegistry: StateUpdaterRegistry<FI, S>,
    private val transformerRegistry: FieldTransformerRegistry<FI> = DefaultTransformerRegistry(),
    private val stateDependencyRegistry: StateDependencyRegistry<FI, S> = DefaultStateDependencyRegistry(),
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
            val fieldState = stateUpdaterRegistry.getFieldState<Any>(fieldId, _state.value)

            if (fieldState.validation == null) {
                updateField(
                    fieldId = fieldId,
                    value = fieldState.value, // Ensure the current value is validated
                )
            }
        }
    }

    // A list can be added, which will show which other fields need to be validated
    // or updated when a specific field is updated
    override fun <T> updateField(
        fieldId: FI,
        value: T?,
        hasFocus: Boolean?,
        shouldHighlightValidationError: Boolean?,
    ) {
        val validation = value?.let {
            validationRegistry.validate(
                fieldId,
                transformerRegistry.transform(fieldId, it),
                _state.value,
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

    override fun updateState(update: S.() -> S) {
        val oldState = _state.value
        _state.update(update)
        val newState = _state.value

        triggerReValidation(oldState, newState)
    }

    private fun triggerReValidation(oldState: S, newState: S) {
        factory.getFieldIds().forEach { fieldId ->
            val dependents = stateDependencyRegistry.getDependencies(fieldId)
            // TODO: Make sure to update the field only once and stop checking other dependents if it is updated
            // TODO: We need to test this
            dependents?.forEach { dependency ->
                val newValue = dependency(newState)
                val oldValue = dependency(oldState)
                if (newValue != oldValue) {
                    val fieldState = stateUpdaterRegistry.getFieldState<Any>(fieldId, _state.value)
                    updateField(fieldId = fieldId, value = fieldState.value)
                    return
                }
            }
        }
    }

    override fun highlightAllFieldValidationErrors() {
        // Flag to focus only the first invalid field
        var isErrorFieldFocused = false

        factory.getFieldIds().forEach { fieldId ->
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
}
