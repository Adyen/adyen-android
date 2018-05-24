package com.adyen.cardscan;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by timon on 31/08/2017.
 */
public final class PaymentCard {
    private String cardNumber;

    private Integer expiryMonth;

    private Integer expiryYear;

    private String securityCode;

    private PaymentCard() {
        // Use builder.
    }

    @Nullable
    public String getCardNumber() {
        return cardNumber;
    }

    @Nullable
    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    @Nullable
    public Integer getExpiryYear() {
        return expiryYear;
    }

    @Nullable
    public String getSecurityCode() {
        return securityCode;
    }

    /**
     * Builder for {@link PaymentCard}s.
     */
    public static final class Builder {
        private PaymentCard mPaymentCard = new PaymentCard();

        @NonNull
        public Builder setCardNumber(@Nullable String cardNumber) {
            mPaymentCard.cardNumber = cardNumber;

            return this;
        }

        @NonNull
        public Builder setExpiryMonth(@Nullable Integer expiryMonth) {
            mPaymentCard.expiryMonth = expiryMonth;

            return this;
        }

        @NonNull
        public Builder setExpiryYear(@Nullable Integer expiryYear) {
            mPaymentCard.expiryYear = expiryYear;

            return this;
        }

        @NonNull
        public Builder setSecurityCode(@Nullable String securityCode) {
            mPaymentCard.securityCode = securityCode;

            return this;
        }

        @NonNull
        public PaymentCard build() {
            return mPaymentCard;
        }
    }
}
