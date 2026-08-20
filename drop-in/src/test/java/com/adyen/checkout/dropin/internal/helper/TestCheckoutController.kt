/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/8/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * A mocked [CheckoutController] with the members the coordinator relies on stubbed.
 *
 * Both stubs matter: an unstubbed [CheckoutController.requiresUserInteraction] defaults to `false`, which sends every
 * flow down the no-UI path, and an unstubbed [CheckoutController.navigation] returns `null`, which throws once the
 * coordinator collects it.
 *
 * @param onSubmit Invoked when the controller is submitted, to observe the state of the world at that moment.
 */
internal fun mockCheckoutController(
    requiresUserInteraction: Boolean = true,
    navigationFlow: Flow<CheckoutRoute> = emptyFlow(),
    onSubmit: () -> Unit = {},
): CheckoutController = mock {
    on { requiresUserInteraction() } doReturn requiresUserInteraction
    on { navigation } doReturn navigationFlow
    on { submit() } doAnswer { onSubmit() }
}
