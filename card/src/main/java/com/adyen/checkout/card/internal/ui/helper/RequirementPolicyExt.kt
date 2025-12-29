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
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

internal fun CVCVisibility.requirementPolicy() = when (this) {
    CVCVisibility.ALWAYS_SHOW -> requiredSecurityCode()
    CVCVisibility.HIDE_FIRST -> RequirementPolicy.Hidden
    CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
}

internal fun StoredCVCVisibility.requirementPolicy() = when (this) {
    StoredCVCVisibility.SHOW -> requiredSecurityCode()
    StoredCVCVisibility.HIDE -> RequirementPolicy.Hidden
}

internal fun DetectedCardType?.expiryDateRequirementPolicy(): RequirementPolicy {
    return when (this?.expiryDatePolicy) {
        Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
        Brand.FieldPolicy.OPTIONAL -> optionalExpiryDate()
        else -> requiredExpiryDate()
    }
}

internal fun DetectedCardType?.securityCodeRequirementPolicy(componentParams: CardComponentParams): RequirementPolicy {
    return if (this?.isReliable == true) {
        when (componentParams.cvcVisibility) {
            CVCVisibility.ALWAYS_SHOW -> {
                when (cvcPolicy) {
                    Brand.FieldPolicy.OPTIONAL -> optionalSecurityCode()
                    Brand.FieldPolicy.HIDDEN -> RequirementPolicy.Hidden
                    else -> requiredSecurityCode()
                }
            }

            CVCVisibility.HIDE_FIRST -> {
                when (cvcPolicy) {
                    Brand.FieldPolicy.REQUIRED -> requiredSecurityCode()
                    Brand.FieldPolicy.OPTIONAL -> optionalSecurityCode()
                    else -> RequirementPolicy.Hidden
                }
            }

            CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
        }
    } else {
        when (componentParams.cvcVisibility) {
            CVCVisibility.ALWAYS_SHOW -> requiredSecurityCode()
            CVCVisibility.HIDE_FIRST -> RequirementPolicy.Hidden
            CVCVisibility.ALWAYS_HIDE -> RequirementPolicy.Hidden
        }
    }
}

internal fun RequirementPolicy?.shouldDisplay() = this !is RequirementPolicy.Hidden
internal fun RequirementPolicy?.label(fallback: CheckoutLocalizationKey): CheckoutLocalizationKey {
    return when (this) {
        is RequirementPolicy.Optional -> this.label
        is RequirementPolicy.Required -> this.label
        else -> null
    } ?: fallback
}

internal fun requiredSecurityCode() = RequirementPolicy.Required(CheckoutLocalizationKey.CARD_SECURITY_CODE)
internal fun optionalSecurityCode() = RequirementPolicy.Optional(CheckoutLocalizationKey.CARD_SECURITY_CODE_OPTIONAL)

internal fun requiredExpiryDate() = RequirementPolicy.Required(CheckoutLocalizationKey.CARD_EXPIRY_DATE)
internal fun optionalExpiryDate() = RequirementPolicy.Optional(CheckoutLocalizationKey.CARD_EXPIRY_DATE_OPTIONAL)
