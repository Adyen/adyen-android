/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2024.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.core.old.PermissionHandlerCallback
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class SessionComponentCallbackTest {

    private val componentCallback = TestSessionComponentCallback()

    @Test
    fun `when onPermissionRequest is called, then onPermissionRequestNotHandled is invoked`() {
        val requiredPermission = "permission"
        val permissionCallback = mock<PermissionHandlerCallback>()

        componentCallback.onPermissionRequest(requiredPermission, permissionCallback)

        verify(permissionCallback).onPermissionRequestNotHandled(eq(requiredPermission))
    }
}
