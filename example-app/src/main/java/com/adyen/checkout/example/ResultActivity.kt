/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/4/2019.
 */

package com.adyen.checkout.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.dropin.DropIn
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        if (intent.hasExtra(DropIn.RESULT_KEY)) {
            resultText.text = intent.getStringExtra(DropIn.RESULT_KEY)
        }
    }
}
