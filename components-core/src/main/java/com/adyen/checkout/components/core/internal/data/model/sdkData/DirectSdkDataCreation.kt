/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/2/2024.
 */

package com.adyen.checkout.components.core.internal.data.model.sdkData

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.provider.SdkDataProvider

/**
 * Marks the constructor of [SdkData] to require an opt-in.
 *
 * Avoid creating [SdkData] directly. Instead, use the [SdkDataProvider] to ensure all fields are
 * populated correctly.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@RequiresOptIn("Avoid creating SdkData directly, use SdkDataProvider instead")
@Target(AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
annotation class DirectSdkDataCreation
