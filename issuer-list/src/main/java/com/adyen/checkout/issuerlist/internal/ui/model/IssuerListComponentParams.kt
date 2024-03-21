/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.internal.ui.model.ComponentParams
import com.adyen.checkout.issuerlist.IssuerListViewType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class IssuerListComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val isSubmitButtonVisible: Boolean,
    val viewType: IssuerListViewType,
    val hideIssuerLogos: Boolean,
) : ComponentParams by commonComponentParams, ButtonParams
