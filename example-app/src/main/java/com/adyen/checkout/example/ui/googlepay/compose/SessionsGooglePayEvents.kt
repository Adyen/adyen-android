/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/5/2024.
 */

package com.adyen.checkout.example.ui.googlepay.compose

internal abstract class SessionsGooglePayEvents internal constructor() {
    object None : SessionsGooglePayEvents()
    class ComponentData(val data: SessionsGooglePayComponentData) : SessionsGooglePayEvents()
    class Action(val action: com.adyen.checkout.components.core.action.Action) : SessionsGooglePayEvents()
    class Intent(val intent: android.content.Intent) : SessionsGooglePayEvents()
}
