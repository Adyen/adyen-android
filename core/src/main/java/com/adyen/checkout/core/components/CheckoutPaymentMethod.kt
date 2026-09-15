/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/4/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.CheckoutFullScreenDialog
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.SecondaryNavigationEvent
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A [Composable] that displays the payment method input UI for the given [controller].
 *
 * Secondary screens of the payment method, such as pickers, are displayed on top of this UI.
 *
 * Use this when you want to render the payment method screen yourself. To render the whole flow
 * (payment method and action screens) automatically, use [CheckoutPaymentFlow] instead.
 *
 * @param controller The [CheckoutController] driving this flow.
 * @param modifier The [Modifier] to be applied to the payment method UI.
 * @param theme The [CheckoutTheme] used to style the UI.
 * @param localizationProvider An optional [CheckoutLocalizationProvider] to override the displayed strings.
 */
@Composable
fun CheckoutPaymentMethod(
    controller: CheckoutController,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    CheckoutCompositionLocalProvider(
        theme = theme,
        locale = controller.shopperLocale,
        localizationProvider = localizationProvider,
        environment = controller.environment,
    ) {
        CheckoutPaymentMethodInternal(controller, modifier)
    }
}

@Composable
internal fun CheckoutPaymentMethodInternal(controller: CheckoutController, modifier: Modifier) {
    val paymentComponent = controller.paymentComponent ?: return
    if (paymentComponent !is SecondaryScreenComponent) {
        paymentComponent.Content(modifier)
        return
    }
    SecondaryScreenHost(paymentComponent, modifier)
}

@Composable
private fun <T> SecondaryScreenHost(
    component: T,
    modifier: Modifier,
) where T : PaymentComponent, T : SecondaryScreenComponent {
    var backStack by rememberSaveable(component) { mutableStateOf(emptyList<String>()) }

    component.Content(modifier)

    if (backStack.isNotEmpty()) {
        CheckoutFullScreenDialog(
            onDismissRequest = { backStack = backStack.dropLast(1) },
        ) {
            AnimatedContent(
                targetState = backStack,
                contentKey = { it.last() },
                transitionSpec = { slideHorizontally() },
            ) { stack ->
                SecondaryScreen(
                    isNested = stack.size > 1,
                    onNavigationClick = { backStack = backStack.dropLast(1) },
                ) {
                    component.SecondaryContent(stack.last(), Modifier)
                }
            }
        }
    }

    LaunchedEffect(component) {
        component.navigation.collect { event ->
            backStack = when (event) {
                is SecondaryNavigationEvent.Open -> {
                    backStack + event.key
                }

                SecondaryNavigationEvent.Close -> {
                    backStack.dropLast(1)
                }
            }
        }
    }
}

@Composable
private fun SecondaryScreen(
    isNested: Boolean,
    onNavigationClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        val (navigationIcon, navigationDescription) = remember {
            if (isNested) {
                Icons.AutoMirrored.Filled.ArrowBack to CheckoutLocalizationKey.GENERAL_BACK
            } else {
                Icons.Default.Close to CheckoutLocalizationKey.GENERAL_CLOSE
            }
        }

        IconButton(onClick = onNavigationClick) {
            Icon(
                imageVector = navigationIcon,
                contentDescription = resolveString(navigationDescription),
                tint = CheckoutThemeProvider.colors.primary,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimensions.Spacing.Large)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }
    }
}

private fun AnimatedContentTransitionScope<List<String>>.slideHorizontally(): ContentTransform {
    return if (targetState.size >= initialState.size) {
        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
    } else {
        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
    }
}
