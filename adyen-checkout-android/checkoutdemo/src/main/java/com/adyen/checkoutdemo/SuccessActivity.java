package com.adyen.checkoutdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class SuccessActivity extends FragmentActivity {

    private Context context;
    private Button backToShopAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_success);
        context = this;

        setStatusBarTranslucent(true);

        backToShopAction = (Button) findViewById(R.id.back_to_shop_action);
        backToShopAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @TargetApi(19)
    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        View v = findViewById(R.id.activity_success);
        if (v != null) {
            int paddingTop = 0;
            TypedValue tv = new TypedValue();
            getTheme().resolveAttribute(0, tv, true);
            paddingTop += TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            v.setPadding(0, makeTranslucent ? paddingTop : 0, 0, 0);
        }

        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

}
