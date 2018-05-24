package com.adyen.utils;

import android.support.annotation.NonNull;


public final class Luhn {

    private Luhn() {
        // private constructor. This class cannot be instantiated.
    }

    /**
     * Luhn check for client-side validation of credit card number.
     */
    public static boolean check(@NonNull final String unformattedNumber) {
        String number = unformattedNumber.replaceAll(" ", "");
        int s1 = 0, s2 = 0;
        String reverse = new StringBuffer(number).reverse().toString();

        for (int i = 0; i < reverse.length(); i++) {
            int digit = Character.digit(reverse.charAt(i), 10);

            if (i % 2 == 0) { //this is for odd digits, they are 1-indexed in the algorithm
                s1 += digit;
            } else { //add 2 * digit for 0-4, add 2 * digit - 9 for 5-9
                s2 += 2 * digit;
                if (digit >= 5) {
                    s2 -= 9;
                }
            }
        }
        return ((s1 + s2) % 10 == 0);
    }
}
