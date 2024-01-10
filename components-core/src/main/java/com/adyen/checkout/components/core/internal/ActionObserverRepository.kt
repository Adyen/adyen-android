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
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ActionObserverRepository(
    private val observerContainer: ObserverContainer = ObserverContainer()
) {

    fun addObservers(
        detailsFlow: Flow<ActionComponentData>?,
        exceptionFlow: Flow<CheckoutException>?,
        permissionFlow: Flow<PermissionRequestParams>?,
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

            permissionFlow?.observe(lifecycleOwner, coroutineScope) { requestParams ->
                callback(
                    ActionComponentEvent.PermissionRequest(
                        requestParams.requiredPermission,
                        requestParams.permissionCallback
                    )
                )
            }
        }
    }

    fun removeObservers() {
        observerContainer.removeObservers()
    }
}
