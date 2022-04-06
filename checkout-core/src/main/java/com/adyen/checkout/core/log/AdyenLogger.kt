/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/4/2022.
 */

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/4/2022.
 */

/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/4/2022.
 */

package com.adyen.checkout.core.log

import android.util.Log
import androidx.annotation.IntDef

private const val SENSITIVE = -1

interface AdyenLogger {

    val sensitive
        get() = SENSITIVE

    @IntDef(SENSITIVE, Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Logger.NONE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class LogLevel

    fun setLogcatLevel(@LogLevel logcatLevel: Int)

    fun v(tag: String, msg: String, tr: Throwable? = null)

    fun d(tag: String, msg: String, tr: Throwable? = null)

    fun i(tag: String, msg: String, tr: Throwable? = null)

    fun w(tag: String, msg: String, tr: Throwable? = null)

    fun e(tag: String, msg: String, tr: Throwable? = null)

    fun sensitiveLog(tag: String, msg: String)
}
