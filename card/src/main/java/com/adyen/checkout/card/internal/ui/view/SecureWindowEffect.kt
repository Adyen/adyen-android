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
    val activitySecureWindow = activity?.window?.let { window ->
        window.peekDecorView()?.let { decorView ->
            secureWindows.getOrPut(decorView) { OwnedSecureWindow(decorView, window) }
        }
    }
    val isActivityRoot = activitySecureWindow?.rootView === rootView
    val hostedSecureWindow = if (isActivityRoot) null else hostedSecureWindowFor(view, rootView)

    return listOfNotNull(hostedSecureWindow, activitySecureWindow)
}

private fun hostedSecureWindowFor(view: View, rootView: View): SecureWindow = secureWindows.getOrPut(rootView) {
    val dialogWindow = findDialogWindowProvider(view.parent)?.window?.takeIf { it.peekDecorView() === rootView }
    if (dialogWindow != null) DialogSecureWindow(rootView, dialogWindow) else UnownedSecureWindow(rootView)
}

private tailrec fun findDialogWindowProvider(parent: ViewParent?): DialogWindowProvider? = when (parent) {
    null -> null
    is DialogWindowProvider -> parent
    else -> findDialogWindowProvider(parent.parent)
}

private abstract class SecureWindow(val rootView: View) {

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
