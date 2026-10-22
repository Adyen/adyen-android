/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/10/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.view.View
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import java.util.WeakHashMap

@Composable
internal fun SecureWindowEffect() {
    val isDebuggable = LocalView.current.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    SecureWindowEffect(isEnabled = !isDebuggable)
}

@Composable
internal fun SecureWindowEffect(isEnabled: Boolean) {
    val view = LocalView.current
    val activity = LocalActivity.current
    if (!isEnabled) return

    DisposableEffect(view, activity) {
        val secureWindow = secureWindowFor(view, activity)
        secureWindow?.acquire()

        onDispose {
            secureWindow?.release()
        }
    }
}

private val secureWindows = WeakHashMap<View, SecureWindow>()

private fun secureWindowFor(view: View, activity: Activity?): SecureWindow? {
    val rootView = view.rootView
    if (rootView.layoutParams !is WindowManager.LayoutParams) return null

    return secureWindows.getOrPut(rootView) {
        val dialogWindow = findDialogWindowProvider(view.parent)?.window?.takeIf { it.peekDecorView() === rootView }
        val activityWindow = activity?.window?.takeIf { it.peekDecorView() === rootView }
        when {
            dialogWindow != null -> DialogSecureWindow(rootView, dialogWindow)
            activityWindow != null -> OwnedSecureWindow(rootView, activityWindow)
            else -> UnownedSecureWindow(rootView)
        }
    }
}

private tailrec fun findDialogWindowProvider(parent: ViewParent?): DialogWindowProvider? = when (parent) {
    null -> null
    is DialogWindowProvider -> parent
    else -> findDialogWindowProvider(parent.parent)
}

private abstract class SecureWindow(protected val rootView: View) {

    private var refCount = 0
    private var wasSecure = false

    fun acquire() {
        if (refCount++ > 0) return
        wasSecure = isSecure()
        if (!wasSecure) setSecure(true)
    }

    fun release() {
        if (--refCount > 0) return
        secureWindows.remove(rootView)
        if (!wasSecure) setSecure(false)
    }

    protected abstract fun isSecure(): Boolean

    protected abstract fun setSecure(secure: Boolean)
}

private open class OwnedSecureWindow(
    rootView: View,
    private val window: Window,
) : SecureWindow(rootView) {

    override fun isSecure() = window.attributes.flags and FLAG_SECURE != 0

    override fun setSecure(secure: Boolean) {
        if (secure) window.addFlags(FLAG_SECURE) else window.clearFlags(FLAG_SECURE)
    }
}

/**
 * A [Window] hosted by a Compose dialog, such as `Dialog` or `ModalBottomSheet`. These hosts reset [FLAG_SECURE] from
 * their own `SecureFlagPolicy` whenever they recompose, which also happens on their own right after being shown. The
 * flag is therefore checked before every draw and reapplied when the host has cleared it.
 */
private class DialogSecureWindow(
    rootView: View,
    window: Window,
) : OwnedSecureWindow(rootView, window), ViewTreeObserver.OnPreDrawListener {

    override fun setSecure(secure: Boolean) {
        if (secure) {
            rootView.viewTreeObserver.addOnPreDrawListener(this)
        } else {
            rootView.viewTreeObserver.removeOnPreDrawListener(this)
        }
        super.setSecure(secure)
    }

    override fun onPreDraw(): Boolean {
        if (!isSecure()) super.setSecure(true)
        return true
    }
}

private class UnownedSecureWindow(
    rootView: View,
) : SecureWindow(rootView), ViewTreeObserver.OnGlobalLayoutListener {

    private val windowManager = rootView.context.getSystemService(WindowManager::class.java)

    override fun isSecure(): Boolean {
        val flags = (rootView.layoutParams as? WindowManager.LayoutParams)?.flags ?: 0
        return flags and FLAG_SECURE != 0
    }

    override fun setSecure(secure: Boolean) {
        if (secure) {
            rootView.viewTreeObserver.addOnGlobalLayoutListener(this)
        } else {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
        updateFlag(secure)
    }

    override fun onGlobalLayout() {
        if (!isSecure()) updateFlag(true)
    }

    private fun updateFlag(secure: Boolean) {
        val params = rootView.layoutParams as? WindowManager.LayoutParams ?: return
        params.flags = if (secure) params.flags or FLAG_SECURE else params.flags and FLAG_SECURE.inv()
        if (rootView.isAttachedToWindow) windowManager.updateViewLayout(rootView, params)
    }
}
