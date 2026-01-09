/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/12/2025.
 */

package com.adyen.checkout.card.internal.ui.helper

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

internal fun CVCVisibility.requirementPolicy() = when (this) {
    CVCVisibility.ALWAYS_SHOW -> RequirementPolicy.Required
    CVCVisibility.HIDE_FIRST -> RequirementPolicy.Hidden
    CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
}

internal fun StoredCVCVisibility.requirementPolicy() = when (this) {
    StoredCVCVisibility.SHOW -> RequirementPolicy.Required
    StoredCVCVisibility.HIDE -> RequirementPolicy.Hidden
}

internal fun DetectedCardType?.expiryDateRequirementPolicy(): RequirementPolicy {
    return when (this?.expiryDatePolicy) {
        Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
        Brand.FieldPolicy.OPTIONAL -> RequirementPolicy.Optional
        else -> RequirementPolicy.Required
    }
}

internal fun DetectedCardType?.securityCodeRequirementPolicy(componentParams: CardComponentParams): RequirementPolicy {
    return if (this?.isReliable == true) {
        when (componentParams.cvcVisibility) {
            CVCVisibility.ALWAYS_SHOW -> {
                when (cvcPolicy) {
                    Brand.FieldPolicy.OPTIONAL -> RequirementPolicy.Optional
                    Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
                    else -> RequirementPolicy.Required
                }
            }

            CVCVisibility.HIDE_FIRST -> {
                when (cvcPolicy) {
                    Brand.FieldPolicy.REQUIRED -> RequirementPolicy.Required
                    Brand.FieldPolicy.OPTIONAL -> RequirementPolicy.Optional
                    else -> RequirementPolicy.Hidden
                }
            }

            CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
        }
    } else {
        when (componentParams.cvcVisibility) {
            CVCVisibility.ALWAYS_SHOW -> RequirementPolicy.Required
            CVCVisibility.HIDE_FIRST -> RequirementPolicy.Hidden
            CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
        }
    }
}

internal fun RequirementPolicy?.shouldDisplay() = this !is RequirementPolicy.Hidden
