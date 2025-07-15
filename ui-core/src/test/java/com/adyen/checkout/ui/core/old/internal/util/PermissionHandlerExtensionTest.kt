/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.adyen.checkout.ui.core.old.TestPermissionHandler
import com.adyen.checkout.ui.core.old.TestPermissionHandlerWithDifferentPermission
import com.adyen.checkout.ui.core.old.TestPermissionHandlerWithNoHandlingForPermissionRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class PermissionHandlerExtensionTest {

    private val permission = "permission"
    private val context = mock<Context>()

    @Test
    fun `when checkPermission is called when permission is already granted, then returns granted result`() =
        runTestWithPermissionInitiallyGranted {
            val permissionHandler = TestPermissionHandler()

            val result = permissionHandler.checkPermission(context, permission)

            assertEquals(PermissionHandlerResult.PERMISSION_GRANTED, result)
        }

    @Test
    fun `when checkPermission is called and permission is granted, then returns granted result`() =
        runTestWithPermissionInitiallyDenied {
            val permissionHandler = TestPermissionHandler(shouldGrantPermission = true)

            val result = permissionHandler.checkPermission(context, permission)

            assertEquals(PermissionHandlerResult.PERMISSION_GRANTED, result)
        }

    @Test
    fun `when checkPermission is called and wrong permission is granted, then returns wrong permission result`() =
        runTestWithPermissionInitiallyDenied {
            val permissionHandler = TestPermissionHandlerWithDifferentPermission()

            val result = permissionHandler.checkPermission(context, permission)

            assertEquals(PermissionHandlerResult.WRONG_PERMISSION, result)
        }

    @Test
    fun `when checkPermission is called and permission is denied, then returns denied result`() =
        runTestWithPermissionInitiallyDenied {
            val permissionHandler = TestPermissionHandler(shouldGrantPermission = false)

            val result = permissionHandler.checkPermission(context, permission)

            assertEquals(PermissionHandlerResult.PERMISSION_DENIED, result)
        }

    @Test
    fun `when checkPermission is called and permission request is not handled, then returns not handled result`() =
        runTestWithPermissionInitiallyDenied {
            val permissionHandler = TestPermissionHandlerWithNoHandlingForPermissionRequest()

            val result = permissionHandler.checkPermission(context, permission)

            assertEquals(PermissionHandlerResult.PERMISSION_REQUEST_NOT_HANDLED, result)
        }

    private fun runTestWithPermissionInitiallyDenied(testBody: suspend TestScope.() -> Unit) = runTest {
        val mockedContextCompat = mockStatic(ContextCompat::class.java)
        whenever(ContextCompat.checkSelfPermission(eq(context), eq(permission))).thenReturn(
            PackageManager.PERMISSION_DENIED,
        )

        testBody.invoke(this)

        mockedContextCompat.close()
    }

    private fun runTestWithPermissionInitiallyGranted(testBody: suspend TestScope.() -> Unit) = runTest {
        val mockedContextCompat = mockStatic(ContextCompat::class.java)
        whenever(ContextCompat.checkSelfPermission(eq(context), eq(permission))).thenReturn(
            PackageManager.PERMISSION_GRANTED,
        )

        testBody.invoke(this)

        mockedContextCompat.close()
    }
}
