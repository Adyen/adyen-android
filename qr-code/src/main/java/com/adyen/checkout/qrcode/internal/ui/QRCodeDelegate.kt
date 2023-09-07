/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/8/2022.
 */

package com.adyen.checkout.qrcode.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.IntentHandlingDelegate
import com.adyen.checkout.components.core.internal.ui.RedirectableDelegate
import com.adyen.checkout.components.core.internal.ui.StatusPollingDelegate
import com.adyen.checkout.components.core.internal.ui.ViewableDelegate
import com.adyen.checkout.qrcode.internal.ui.model.QRCodeOutputData
import com.adyen.checkout.qrcode.internal.ui.model.QrCodeUIEvent
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface QRCodeDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    ViewableDelegate<QRCodeOutputData>,
    IntentHandlingDelegate,
    StatusPollingDelegate,
    ViewProvidingDelegate,
    RedirectableDelegate {

    val eventFlow: Flow<QrCodeUIEvent>

    fun downloadQRImage()
}
