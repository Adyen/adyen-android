package com.adyen.checkout;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class provides a list of creditcard numbers that can be used on Test.
 * See: https://gist.github.com/j3j5/8b3e48ccad746b90a54a
 */

public final class CardNumberService {

    private CardNumberService() {
        // Private Constructor
    }

    public static Collection<String> getMasterCardNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("5100 0811 1222 3332");
        list.add("5100 2900 2900 2909");
        list.add("5577 0000 5577 0004");
        list.add("5136 3333 3333 3335");
        list.add("5585 5585 5585 5583");
        list.add("5555 4444 3333 1111");
        list.add("5555 5555 5555 4444");
        list.add("5500 0000 0000 0004");
        list.add("5424 0000 0000 0015");
        return list;
    }

    public static Collection<String> getVISANumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("4111 1111 1111 1111");
        list.add("4988 4388 4388 4305");
        list.add("4166 6766 6766 6746");
        list.add("4646 4646 4646 4644");
        list.add("4444 3333 2222 1111");
        list.add("4400 0000 0000 0008");
        list.add("4977 9494 9494 9497");
        return list;
    }

    public static Collection<String> getJCBNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("3569 9900 1009 5841");
        return list;
    }

    public static Collection<String> getCarteBancaireNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("4035 5010 0000 0008");
        list.add("4360 0000 0100 0005");
        return list;
    }

    public static Collection<String> getAmexNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("3700 0000 0000 002");
        return list;
    }

    public static Collection<String> getDinersNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("3600 6666 3333 44");
        return list;
    }

    public static Collection<String> gerDiscoverNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("6011 6011 6011 6611");
        list.add("6445 6445 6445 6445");
        return list;
    }

    public static Collection<String> getMaestroNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("6731 0123 4567 8906");
        list.add("6759 6498 2643 8453");
        return list;
    }

    public static Collection<String> getMisterCashNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("6703 4444 4444 4449");
        return list;
    }

    public static Collection<String> getHiperCardNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("6062 8288 8866 6688");
        return list;
    }

    public static Collection<String> getBrazilianCompanyNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("5066 9911 1111 1118");
        return list;
    }

    public static Collection<String> getDankortNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("5019 5555 4444 5555");
        return list;
    }

    public static Collection<String> getUnionPayCreditCardNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("6221 5588 1234 0000");
        return list;
    }

    public static Collection<String> get3DSecureNumbers() {
        ArrayList<String> list = new ArrayList<>();
        list.add("5212 3456 7890 1234");
        list.add("4212 3456 7890 1237");
        list.add("3451 7792 5488 348");
        list.add("3569 9900 1009 5833");
        return list;
    }

    public static Collection<String> removeWhiteSpaces(Collection<String> collection) {
        ArrayList<String> result = new ArrayList<>();
        for (String str : collection) {
            result.add(str.replaceAll(" ", ""));
        }
        return result;
    }
}

