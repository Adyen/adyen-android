/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/8/2022.
 */

package com.adyen.checkout.await.old.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.await.old.internal.ui.model.AwaitOutputData
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.RedirectableDelegate
import com.adyen.checkout.components.core.internal.ui.StatusPollingDelegate
import com.adyen.checkout.components.core.internal.ui.ViewableDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AwaitDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    ViewableDelegate<AwaitOutputData>,
    StatusPollingDelegate,
    ViewProvidingDelegate,
    RedirectableDelegate
