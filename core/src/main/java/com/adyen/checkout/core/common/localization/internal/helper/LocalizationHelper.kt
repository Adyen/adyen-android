/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/9/2025.
 */

package com.adyen.checkout.core.common.localization.internal.helper

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import com.adyen.checkout.core.common.internal.helper.LocalLocale
import com.adyen.checkout.core.common.internal.helper.LocalLocalizationResolver
import com.adyen.checkout.core.common.internal.helper.currentLocalizedContext
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun resolveString(key: CheckoutLocalizationKey): String {
    val localizationResolver = LocalLocalizationResolver.current
    return localizationResolver.getLocalizedStringFor(
        context = currentLocalizedContext,
        locale = LocalLocale.current,
        key = key,
    )
}
