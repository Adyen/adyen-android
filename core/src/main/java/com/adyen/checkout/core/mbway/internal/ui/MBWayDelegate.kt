/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.internal.ui.state.DefaultDelegateStateManager
import com.adyen.checkout.core.internal.ui.state.DelegateStateManager
import com.adyen.checkout.core.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayStateUpdaterRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayTransformerRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayValidatorRegistry
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.core.mbway.internal.ui.view.MbWayComponent

internal class MBWayDelegate : PaymentDelegate, FieldChangeListener<MBWayFieldId> {

    private val stateManager: DelegateStateManager<MBWayDelegateState, MBWayFieldId> =
        createStateManager()

    override fun submit() = if (stateManager.isValid) {
        // TODO Implement the submit logic
    } else {
        stateManager.highlightAllFieldValidationErrors()
    }

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        MbWayComponent(
            fieldChangeListener = this,
        )
    }

    override fun <T> onFieldValueChanged(
        fieldId: MBWayFieldId,
        value: T
    ) = stateManager.updateFieldValue(fieldId, value)

    override fun onFieldFocusChanged(
        fieldId: MBWayFieldId,
        hasFocus: Boolean
    ) = stateManager.updateFieldFocus(fieldId, hasFocus)

    private fun createStateManager(): DelegateStateManager<MBWayDelegateState, MBWayFieldId> {
        val transformerRegistry = MBWayTransformerRegistry()

        val delegateStateFactory = MBWayDelegateStateFactory()

        return DefaultDelegateStateManager(
            factory = delegateStateFactory,
            validationRegistry = MBWayValidatorRegistry(),
            stateUpdaterRegistry = MBWayStateUpdaterRegistry(),
            transformerRegistry = transformerRegistry,
        )
    }
}
