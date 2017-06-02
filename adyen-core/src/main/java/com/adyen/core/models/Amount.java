package com.adyen.core.models;

import java.io.Serializable;

/**
 * Representation of an amount.
 * Contains the value in minor units (e.g. 200 for $2)
 * and the String representation of the currency.
 */
public class Amount implements Serializable {

    private static final long serialVersionUID = 1207109757321515976L;
    private String currency;
    /**
     * The value set in minor units (e.g. for $2 we set 200).
     */
    private long value;

    /**
     *
     * @param value The value of the amount in minor units.
     * @param currency String representation of the currency ("EUR", "USD", ...)
     */
    public Amount(final long value, final String currency) {
        this.value = value;
        this.currency = currency;
    }

    /**
     * Currency of the amount.
     * @return Currency in string representation (e.g. "EUR", "USD", ...)
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Gets the value of the amount in minor units (e.g. for $2 we return 200).
     * @return The value of the amount in minor units.
     */
    public long getValue() {
        return value;
    }

}
