/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/10/2026.
 */

package com.adyen.checkout.core.action.internal

import androidx.annotation.RestrictTo

/**
 * An [ActionComponent] that can resume its action after process death.
 *
 * Action components that do not implement this interface cannot be restored, and the checkout flow fails instead.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface RestorableActionComponent {

    /**
     * Resumes the action this component was created with, instead of [ActionComponent.handleAction]. The action must
     * not be launched again, for example a redirect must not be opened a second time.
     *
     * Initialize any state from the action itself, not from the values already in the saved state handle. Those
     * values are shared by every flow that uses the same handle, so another flow may have overwritten them.
     */
    fun restoreAction()
}
