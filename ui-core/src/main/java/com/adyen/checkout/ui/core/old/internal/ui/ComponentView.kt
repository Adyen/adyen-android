/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2022.
 */
package com.adyen.checkout.ui.core.old.internal.ui

import android.content.Context
import android.view.View
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import kotlinx.coroutines.CoroutineScope

/**
 * A View that can display input and fill in details for a Component.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentView {
    /**
     * This function will be called when the component is attached and the view is ready to get initialized.
     */
    fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context)

    /**
     * Highlight and focus on the current validation errors for the user to take action.
     * If the component doesn't need validation or if everything is already valid, nothing will happen.
     */
    fun highlightValidationErrors()

    fun getView(): View
}
