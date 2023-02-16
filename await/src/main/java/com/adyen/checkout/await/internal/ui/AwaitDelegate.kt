/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/8/2022.
 */

package com.adyen.checkout.await.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.await.internal.ui.model.AwaitOutputData
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.StatusPollingDelegate
import com.adyen.checkout.components.base.ViewableDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AwaitDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    ViewableDelegate<AwaitOutputData>,
    StatusPollingDelegate,
    ViewProvidingDelegate
