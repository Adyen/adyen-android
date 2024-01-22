/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/1/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.adyen.checkout.ui.core.TestPermissionHandler
import com.adyen.checkout.ui.core.TestPermissionHandlerWithDifferentPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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
    fun `test checkPermission when permission is already granted`() = runTestWithPermissionInitiallyGranted {
        val permissionHandler = TestPermissionHandler()

        val result = permissionHandler.checkPermission(context, permission)

        assertTrue(result!!)
    }

    @Test
    fun `test checkPermission when permission is granted`() = runTestWithPermissionInitiallyDenied {
        val permissionHandler = TestPermissionHandler(shouldGrantPermission = true)

        val result = permissionHandler.checkPermission(context, permission)

        assertTrue(result!!)
    }

    @Test
    fun `test checkPermission when wrong permission is granted`() = runTestWithPermissionInitiallyDenied {
        val permissionHandler = TestPermissionHandlerWithDifferentPermission()

        val result = permissionHandler.checkPermission(context, permission)

        assertNull(result)
    }

    @Test
    fun `test checkPermission when permission is denied`() = runTestWithPermissionInitiallyDenied {
        val permissionHandler = TestPermissionHandler(shouldGrantPermission = false)

        val result = permissionHandler.checkPermission(context, permission)

        assertFalse(result!!)
    }

    private fun runTestWithPermissionInitiallyDenied(testBody: suspend TestScope.() -> Unit) = runTest {
        val mockedContextCompat = mockStatic(ContextCompat::class.java)
        whenever(ContextCompat.checkSelfPermission(eq(context), eq(permission))).thenReturn(
            PackageManager.PERMISSION_DENIED
        )

        testBody.invoke(this)

        mockedContextCompat.close()
    }

    private fun runTestWithPermissionInitiallyGranted(testBody: suspend TestScope.() -> Unit) = runTest {
        val mockedContextCompat = mockStatic(ContextCompat::class.java)
        whenever(ContextCompat.checkSelfPermission(eq(context), eq(permission))).thenReturn(
            PackageManager.PERMISSION_GRANTED
        )

        testBody.invoke(this)

        mockedContextCompat.close()
    }
}
