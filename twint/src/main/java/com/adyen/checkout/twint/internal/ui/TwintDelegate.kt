/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2023.
 */

package com.adyen.checkout.twint.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.StatusPollingDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface TwintDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    StatusPollingDelegate,
    ViewProvidingDelegate
