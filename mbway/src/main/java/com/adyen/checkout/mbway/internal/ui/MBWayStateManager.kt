/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.components.core.internal.ui.model.field.StateManager
import com.adyen.checkout.components.core.internal.ui.model.field.StateUpdaterRegistry
import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry
import com.adyen.checkout.components.core.internal.ui.model.updateFieldState
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId

internal class MBWayStateManager(
    private val transformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    private val validationRegistry: FieldValidatorRegistry<MBWayFieldId>,
    private val stateUpdaterRegistry: StateUpdaterRegistry<MBWayFieldId, MBWayDelegateState>,
) : StateManager<MBWayDelegateState, MBWayFieldId> {

    override fun <T> updateField(
        state: MBWayDelegateState,
        fieldId: MBWayFieldId,
        value: T?,
        hasFocus: Boolean?,
        shouldHighlightValidationError: Boolean?,
    ): MBWayDelegateState {
        val validation = value?.let {
            validationRegistry.validate(
                fieldId,
                transformerRegistry.transform(fieldId, value),
            )
        }

        val fieldState = stateUpdaterRegistry.getFieldState<T>(fieldId, state)
        val updatedFieldState = fieldState.updateFieldState(
            value = value,
            validation = validation,
            hasFocus = hasFocus,
            shouldHighlightValidationError = shouldHighlightValidationError,
        )

        return stateUpdaterRegistry.updateFieldState(fieldId, state, updatedFieldState)
    }
}
