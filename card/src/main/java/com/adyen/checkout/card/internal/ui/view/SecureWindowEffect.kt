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
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

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
        val windows = secureWindowsFor(view, activity)
        windows.forEach { it.acquire() }

        onDispose {
            windows.forEach { it.release() }
        }
    }
}

// A WeakHashMap would not help here, because every SecureWindow strongly references its root view, which is also the
// key. Entries are removed in SecureWindow.release() instead, which DisposableEffect guarantees to call.
private val secureWindows = mutableMapOf<View, SecureWindow>()

private fun secureWindowsFor(view: View, activity: Activity?): List<SecureWindow> {
    val rootView = view.rootView
    if (rootView.layoutParams !is WindowManager.LayoutParams) return emptyList()

    // The activity window is secured even when the card is rendered in a dialog or popup. These hosts copy FLAG_SECURE
    // from the window they are hosted in whenever they recompose, if they use the default SecureFlagPolicy.Inherit.
    // Otherwise they clear the flag, and it only gets reapplied after the window manager has already received it.
    val activityWindow = activity?.window
    val activityRootView = activityWindow?.peekDecorView()
    val activitySecureWindow = activityRootView?.let { secureWindowFor(it, activityWindow) }
    val isActivityRoot = rootView === activityRootView
    val hostedSecureWindow = if (isActivityRoot) null else secureWindowFor(rootView, dialogWindowOf(view))

    return listOfNotNull(hostedSecureWindow, activitySecureWindow)
}

private fun secureWindowFor(rootView: View, window: Window?) = secureWindows.getOrPut(rootView) {
    SecureWindow(rootView, window)
}

private fun dialogWindowOf(view: View): Window? = generateSequence(view.parent) { it.parent }
    .filterIsInstance<DialogWindowProvider>()
    .firstOrNull()
    ?.window
    ?.takeIf { it.peekDecorView() === view.rootView }

/**
 * Secures the window of [rootView], through [window] when there is one and through the layout params of [rootView]
 * otherwise, such as for View-based dialogs. Hosts like Compose `Dialog` and `ModalBottomSheet` reset [FLAG_SECURE]
 * from their own `SecureFlagPolicy` whenever they recompose, which also happens on their own right after being shown.
 * The flag is therefore checked before every draw and reapplied when the host has cleared it.
 */
private class SecureWindow(
    private val rootView: View,
    private val window: Window?,
) : ViewTreeObserver.OnPreDrawListener {

    private var refCount = 0
    private var wasSecure = false

    fun acquire() {
        if (refCount++ > 0) return
        wasSecure = isSecure()
        if (!wasSecure) {
            rootView.viewTreeObserver.addOnPreDrawListener(this)
            setSecure(true)
        }
    }

    fun release() {
        if (--refCount > 0) return
        secureWindows.remove(rootView)
        if (!wasSecure) {
            rootView.viewTreeObserver.removeOnPreDrawListener(this)
            setSecure(false)
        }
    }

    override fun onPreDraw(): Boolean {
        if (!isSecure()) setSecure(true)
        return true
    }

    private fun isSecure(): Boolean {
        val flags = window?.attributes?.flags ?: (rootView.layoutParams as? WindowManager.LayoutParams)?.flags ?: 0
        return flags and FLAG_SECURE != 0
    }

    private fun setSecure(secure: Boolean) {
        if (window != null) {
            if (secure) window.addFlags(FLAG_SECURE) else window.clearFlags(FLAG_SECURE)
        } else {
            val params = rootView.layoutParams as? WindowManager.LayoutParams ?: return
            params.flags = if (secure) params.flags or FLAG_SECURE else params.flags and FLAG_SECURE.inv()
            if (rootView.isAttachedToWindow) {
                rootView.context.getSystemService(WindowManager::class.java).updateViewLayout(rootView, params)
            }
        }
    }
}
