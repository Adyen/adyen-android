/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.adyen3ds2.internal.data.model

import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.Threeds2Action
import org.json.JSONObject

internal sealed class SubmitFingerprintResult {
    class Completed(val details: JSONObject) : SubmitFingerprintResult()
    class Redirect(val action: RedirectAction) : SubmitFingerprintResult()
    class Threeds2(val action: Threeds2Action) : SubmitFingerprintResult()
}
