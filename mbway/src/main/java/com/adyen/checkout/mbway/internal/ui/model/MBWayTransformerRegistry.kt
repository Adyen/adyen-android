/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformer
import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry

internal class MBWayTransformerRegistry : FieldTransformerRegistry<MBWayFieldId> {
    private val transformers = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.LOCAL_PHONE_NUMBER -> LocalPhoneNumberTransformer()
            MBWayFieldId.COUNTRY_CODE -> DefaultTransformer()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> transform(key: MBWayFieldId, value: T): T {
        val transformer = transformers[key] as? FieldTransformer<T>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return transformer.transform(value)
    }
}

internal class LocalPhoneNumberTransformer : FieldTransformer<String> {
    override fun transform(value: String) = value.trimStart('0')
}

internal class DefaultTransformer : FieldTransformer<Any> {
    override fun transform(value: Any) = value
}
