/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2025.
 */

package com.adyen.checkout.core.components.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("LongParameterList")
fun generateComponentParamsBundle(
    shopperLocale: Locale = Locale("nl", "NL"),
    environment: Environment = Environment.TEST,
    clientKey: String = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
    analyticsParams: AnalyticsParams = AnalyticsParams(
        AnalyticsParamsLevel.ALL,
        "test_qwertyuiopasdfghjklzxcvbnmqwerty",
    ),
    isCreatedByDropIn: Boolean = false,
    amount: Amount? = null,
    isSubmitButtonVisible: Boolean = true,
) = ComponentParamsBundle(
    commonComponentParams = generateCommonComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        analyticsParams = analyticsParams,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        isSubmitButtonVisible = isSubmitButtonVisible,
    ),
    sessionParams = null,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("LongParameterList")
fun generateCommonComponentParams(
    shopperLocale: Locale = Locale("nl", "NL"),
    environment: Environment = Environment.TEST,
    clientKey: String = "test_qwertyuiopasdfghjklzxcvbnmqwerty",
    analyticsParams: AnalyticsParams = AnalyticsParams(
        AnalyticsParamsLevel.ALL,
        "test_qwertyuiopasdfghjklzxcvbnmqwerty",
    ),
    isCreatedByDropIn: Boolean = false,
    amount: Amount? = null,
    isSubmitButtonVisible: Boolean = true,
    publicKey: String = TEST_PUBLIC_KEY
) = CommonComponentParams(
    shopperLocale = shopperLocale,
    environment = environment,
    clientKey = clientKey,
    analyticsParams = analyticsParams,
    isCreatedByDropIn = isCreatedByDropIn,
    amount = amount,
    isSubmitButtonVisible = isSubmitButtonVisible,
    publicKey = publicKey,
)

private const val TEST_PUBLIC_KEY =
    "10001|1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
        "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
        "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
        "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
        "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
        "1111111111111111111111111111111111"
