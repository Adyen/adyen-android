package com.adyen.checkout.ui.internal.card;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.card.Cards;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.Card;
import com.adyen.checkout.core.model.CardDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.fragment.CheckoutDetailsFragment;
import com.adyen.checkout.ui.internal.common.model.CheckoutHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 10/04/2018.
 */
abstract class CardCheckoutMethod extends CheckoutMethod {
    private CardCheckoutMethod(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        super(application, paymentMethod);
    }

    public static final class OneClick extends CardCheckoutMethod {
        private final Card mCard;

        OneClick(@NonNull Application application, @NonNull PaymentMethod paymentMethod, @NonNull Card card) {
            super(application, paymentMethod);

            mCard = card;
        }

        @NonNull
        @Override
        public String getPrimaryText() {
            return Cards.FORMATTER.maskNumber(mCard.getLastFourDigits());
        }

        @NonNull
        @Override
        public String getSecondaryText() {
            String formattedExpiryDate = Cards.FORMATTER.formatExpiryDate(mCard.getExpiryMonth(), mCard.getExpiryYear());

            return getApplication().getString(R.string.checkout_card_one_click_expires_format, formattedExpiryDate);
        }

        @Override
        public void onSelected(@NonNull CheckoutHandler checkoutHandler) {
            PaymentReference paymentReference = checkoutHandler.getPaymentReference();
            PaymentMethod paymentMethod = getPaymentMethod();

            if (InputDetailImpl.findByKey(paymentMethod.getInputDetails(), CardDetails.KEY_PHONE_NUMBER) == null) {
                CheckoutDetailsFragment fragment = CardOneClickConfirmationFragment.newInstance(paymentReference, paymentMethod);
                checkoutHandler.presentDetailsFragment(fragment);
            } else {
                Intent intent = CupSecurePlusOneClickDetailsActivity.newIntent(getApplication(), paymentReference, paymentMethod);
                checkoutHandler.presentDetailsActivity(intent);
            }
        }
    }

    public static final class Default extends CardCheckoutMethod {
        Default(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            super(application, paymentMethod);
        }

        @Override
        public void onSelected(@NonNull CheckoutHandler checkoutHandler) {
            PaymentReference paymentReference = checkoutHandler.getPaymentReference();
            PaymentMethodHandler paymentMethodHandler = new CardHandler(paymentReference, getPaymentMethod());
            checkoutHandler.handleWithPaymentMethodHandler(paymentMethodHandler);
        }
    }
}
