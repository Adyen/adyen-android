package com.adyen.utils;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.regex.Pattern;


public enum CardType {

    amex("^3[47][0-9]{0,13}$", 15),
    diners("^(36)[0-9]{0,12}$", 14),
    discover("^(6011[0-9]{0,12}|(644|645|646|647|648|649)[0-9]{0,13}|65[0-9]{0,14})$", 16),
    elo("^((((506699)|(506770)|(506771)|(506772)|(506773)|(506774)|(506775)|(506776)|(506777)|(506778)"
            + "|(401178)|(438935)|(451416)|(457631)|(457632)|(504175)|(627780)|(636368)|(636297))[0-9]"
            + "{0,10})|((50676)|(50675)|(50674)|(50673)|(50672)|(50671)|(50670))[0-9]{0,11})$", 16),
    hipercard("^(606282)[0-9]{0,10}$", 16),
    jcb("^(352[8,9]{1}[0-9]{0,15}|35[4-8]{1}[0-9]{0,16})$", 16, 19),
    bijcard("^(5100081)[0-9]{0,9}$", 16), // FIXME: Conflict with multiple card types (visa, mastercard etc)
    maestrouk("^(6759)[0-9]{0,15}$", 16, 18, 19),
    solo("^(6767)[0-9]{0,15}$", 16, 18, 19),
    bcmc("^((6703)[0-9]{0,15}|(479658|606005)[0-9]{0,13})$", 16, 17, 18, 19),
    dankort("^(5019)[0-9]{0,12}$", 16),
    uatp("^1[0-9]{0,14}$", 15),
    cup("^(62)[0-9]{0,17}$", 14, 15, 16, 17, 18, 19),
    codensa("^(590712)[0-9]{0,10}$", 16),
    visaalphabankbonus("^(450903)[0-9]{0,10}$", 16),
    visadankort("^(4571)[0-9]{0,12}$", 16),
    mcalphabankbonus("^(510099)[0-9]{0,10}$", 16),
    hiper("^(637095|637599|637609|637612)[0-9]{0,10}$", 16),
    oasis("^(982616)[0-9]{0,10}$", 16),
    karenmillen("^(98261465)[0-9]{0,8}$", 16),
    warehouse("^(982633)[0-9]{0,10}$", 16),
    mir("^(220)[0-9]{0,16}$", 16, 17, 18, 19),
    visa("^4[0-9]{0,16}$", 13, 16),
    mc("^(5[1-5][0-9]{0,14}|2[2-7][0-9]{0,14})$", 16),
    maestro("^(5[6-8][0-9]{0,17}|6[0-9]{0,18})$", 16, 17, 18, 19),
    cartebancaire("^[4-6][0-9]{0,15}$", 16), // FIXME: Conflict with multiple card types (visa, mastercard etc)
    UNKNOWN("", -1);

    private Pattern pattern;
    private Integer[] numberOfDigits;

    CardType(String pattern, Integer... length) {
        this.pattern = Pattern.compile(pattern);
        this.numberOfDigits = length;
    }

    public static CardType detect(final String cardNumber, @NonNull final List<String> availableCardMethods) {
        for (CardType cardType : CardType.values()) {
            if (null == cardType.pattern) {
                continue;
            }

            if (cardType.pattern.matcher(cardNumber).matches() && availableCardMethods.contains(cardType.toString())) {
                return cardType;
            }
        }
        return UNKNOWN;
    }

    public Integer[] getNumberOfDigits() {
        final Integer[] copyOfNumberOfDigits = new Integer[numberOfDigits.length];
        System.arraycopy(numberOfDigits, 0, copyOfNumberOfDigits, 0, numberOfDigits.length);
        return copyOfNumberOfDigits;
    }
}
