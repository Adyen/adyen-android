package com.adyen.checkout.redirect

import android.app.Activity
import android.content.ActivityNotFoundException
import android.net.Uri
import android.text.TextUtils
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONObject

private val TAG = LogUtil.getTag()

class RedirectDelegate {

    /**
     * Make a redirect from the provided Activity to the target of the Redirect object.
     *
     * @param activity The Activity starting the redirect.
     * @param redirectAction The object from the server response defining where to redirect to.
     */
    fun makeRedirect(activity: Activity, redirectAction: RedirectAction) {
        Logger.d(TAG, "makeRedirect - " + redirectAction.url)
        if (!TextUtils.isEmpty(redirectAction.url)) {
            val redirectUri = Uri.parse(redirectAction.url)
            val redirectIntent = RedirectUtil.createRedirectIntent(activity, redirectUri)
            try {
                activity.startActivity(redirectIntent)
            } catch (e: ActivityNotFoundException) {
                throw ComponentException("Redirect to app failed.", e)
            }
        } else {
            throw ComponentException("Redirect URL is empty.")
        }
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param data The Uri from the response.
     */
    fun handleRedirectResponse(data: Uri?): JSONObject {
        if (data == null) throw ComponentException("Received a null redirect Uri")
        return RedirectUtil.parseRedirectResult(data)
    }
}
