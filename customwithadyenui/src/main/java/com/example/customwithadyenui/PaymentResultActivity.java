package com.example.customwithadyenui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity for displaying the result.
 */

public class PaymentResultActivity extends Activity {

    private String result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification_activity);
        result = getIntent().getStringExtra("Result");
        ((TextView) findViewById(R.id.verificationTextView)).setText(result);
    }
}
