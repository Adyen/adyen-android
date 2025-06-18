/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/11/2022.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.core.old.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ActionObserverRepository(
    private val observerContainer: ObserverContainer = ObserverContainer()
) {

    @Suppress("LongParameterList")
    fun addObservers(
        detailsFlow: Flow<ActionComponentData>?,
        exceptionFlow: Flow<CheckoutException>?,
        permissionFlow: Flow<PermissionRequestData>?,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit,
    ) {
        with(observerContainer) {
            removeObservers()

            detailsFlow?.observe(lifecycleOwner, coroutineScope) { componentData ->
                callback(ActionComponentEvent.ActionDetails(componentData))
            }

            exceptionFlow?.observe(lifecycleOwner, coroutineScope) { exception ->
                callback(ActionComponentEvent.Error(ComponentError(exception)))
            }

            permissionFlow?.observe(lifecycleOwner, coroutineScope) { requestData ->
                callback(
                    ActionComponentEvent.PermissionRequest(
                        requestData.requiredPermission,
                        requestData.permissionCallback
                    )
                )
            }
        }
    }

    fun removeObservers() {
        observerContainer.removeObservers()
    }
}
