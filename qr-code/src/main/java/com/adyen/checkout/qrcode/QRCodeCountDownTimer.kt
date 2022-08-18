/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/8/2022.
 */

package com.adyen.checkout.qrcode

import android.os.CountDownTimer

internal class QRCodeCountDownTimer {

    private lateinit var countDownTimer: CountDownTimer

    fun attach(
        millisInFuture: Long,
        countDownInterval: Long,
        onTick: (millisUntilFinished: Long) -> Unit,
    ) {
        countDownTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                onTick(millisUntilFinished)
            }

            override fun onFinish() = Unit
        }
    }

    fun start() {
        countDownTimer.start()
    }

    fun cancel() {
        countDownTimer.cancel()
    }
}
