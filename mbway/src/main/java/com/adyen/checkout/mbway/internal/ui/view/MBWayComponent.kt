/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.mbway.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.NewCheckoutController
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.mbway.internal.ui.MBWayViewModel
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import java.util.Locale

@Composable
internal fun MBWayComponent(
    modifier: Modifier,
    controller: NewCheckoutController,
) {
    val viewModel = viewModel {
        // TODO - Use actual component params
        val componentParams = CommonComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = "",
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            isCreatedByDropIn = false,
            amount = null,
            showSubmitButton = true,
            publicKey = "",
        )
        MBWayViewModel(
            controller = controller,
            componentStateFactory = MBWayComponentStateFactory(componentParams),
            componentStateReducer = MBWayComponentStateReducer(),
            componentStateValidator = MBWayComponentStateValidator(),
            viewStateProducer = MBWayViewStateProducer(),
        )
    }
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    MBWayContent(
        viewState = viewState,
        onIntent = viewModel::onIntent,
        onSubmitClick = viewModel::submit,
        onCountryCodePickerClick = { TODO() },
        modifier = modifier,
    )
}
