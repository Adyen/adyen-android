/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay.compose

import android.content.Intent
import com.adyen.checkout.components.core.action.Action

internal abstract class SessionsGooglePayEvents internal constructor() {
    object None : SessionsGooglePayEvents()
    class ComponentData(val data: SessionsGooglePayComponentData) : SessionsGooglePayEvents()
    class WithAction(val action: Action) : SessionsGooglePayEvents()
    class WithIntent(val intent: Intent) : SessionsGooglePayEvents()
}
