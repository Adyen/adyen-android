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
import com.adyen.checkout.example.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val result = DropIn.getDropInResultFromIntent(intent)
        if (result != null) {
            binding.resultText.text = result
        }
    }
}
