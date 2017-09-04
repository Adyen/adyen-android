package com.adyen.ui.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.adyen.core.constants.Constants;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.adyen.core.utils.AmountUtil;
import com.adyen.core.utils.StringUtils;
import com.adyen.ui.R;
import com.adyen.ui.utils.AdyenInputValidator;
import com.adyen.utils.CardType;

public class CVCDialog extends Dialog {

    private Amount amount;
    private PaymentMethod paymentMethod;
    private boolean backButtonEnabled = true;
    private CVCDialogListener cvcDialogListener;

    private Activity activity;

    public interface CVCDialogListener {
        void onDismissed();
    }

    public CVCDialog(Activity activity, Amount amount, PaymentMethod paymentMethod,
                     CVCDialogListener cvcDialogListener) {
        super(activity, R.style.dialogStyle);
        this.activity = activity;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.cvcDialogListener = cvcDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cvc_dialog);

        setCanceledOnTouchOutside(false);

        final TextView cvcHintTextView = ((TextView) findViewById(R.id.extended_cvc_hint_textview));
        final String cardName = paymentMethod.getName().replaceAll(" ", "\u00A0"); //replace whitespace
        // in "**** 1234" with non-breakable whitespace
        final String cvcHint = getContext().getString(R.string.cvc_extended_hint_with_last_digits, cardName);
        cvcHintTextView.setText(cvcHint);

        final CVCEditText cvcEditText = (CVCEditText) findViewById(R.id.adyen_credit_card_cvc);

        final Button cancelButton = ((Button) findViewById(R.id.button_cancel_cvc_verification));

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvcDialogListener.onDismissed();
            }
        });

        AdyenInputValidator validator = new AdyenInputValidator();
        final Button checkoutButton = (Button) findViewById(R.id.button_confirm_cvc_verification);
        String amountStr = AmountUtil.format(amount, true, StringUtils.getLocale(activity));
        final String amountString = getContext().getString(R.string.pay_with_amount, amountStr);
        checkoutButton.setText(amountString);
        checkoutButton.setEnabled(false);
        validator.setOnReadyStateChangedListener(new AdyenInputValidator.OnReadyStateChangedListener() {
            @Override
            public void onReadyStateChanged(boolean isReady) {
                checkoutButton.setEnabled(isReady);
            }
        });
        cvcEditText.setValidator(validator);

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cvcEditText.hasValidInput()) {
                    return;
                } else {
                    final Intent intent = new Intent(Constants.PaymentRequest.PAYMENT_DETAILS_PROVIDED_INTENT);
                    PaymentDetails paymentDetails = new PaymentDetails(paymentMethod.getInputDetails());
                    paymentDetails.fill("cardDetails.cvc", cvcEditText.getCVC());
                    intent.putExtra("PaymentDetails", paymentDetails);
                    LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(intent);

                    cvcEditText.setEnabled(false);
                    checkoutButton.setEnabled(false);
                    cancelButton.setEnabled(false);

                    backButtonEnabled = false;

                    findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                }
            }
        });

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(cvcEditText, InputMethodManager.SHOW_FORCED);
                cvcEditText.requestFocus();
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (activity.getWindow() != null) {
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });

        if (paymentMethod.getType().equals(CardType.amex.toString())) {
            cvcEditText.setMaxLength(CVCEditText.CVC_MAX_LENGTH_AMEX);
        }

    }

    @Override
    public void onBackPressed() {
        if (backButtonEnabled) {
            activity.finish();
        }
    }
}
