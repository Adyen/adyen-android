/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.core.components.internal.ui.state.transformer.DefaultTransformer
import com.adyen.checkout.core.components.internal.ui.state.transformer.FieldTransformer
import com.adyen.checkout.core.components.internal.ui.state.transformer.FieldTransformerRegistry
import com.adyen.checkout.mbway.internal.ui.state.MBWayFieldId

internal class MBWayTransformerRegistry : FieldTransformerRegistry<MBWayFieldId> {
    private val transformers = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.COUNTRY_CODE -> DefaultTransformer()
            MBWayFieldId.PHONE_NUMBER -> LocalPhoneNumberTransformer()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> transform(fieldId: MBWayFieldId, value: T): T {
        val transformer = transformers[fieldId] as? FieldTransformer<T>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return transformer.transform(value)
    }
}

internal class LocalPhoneNumberTransformer : FieldTransformer<String> {
    override fun transform(value: String) = value.trimStart('0')
}
