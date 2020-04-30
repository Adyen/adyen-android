/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/9/2019.
 */

package com.adyen.checkout.card.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public enum CardType {
    AMERICAN_EXPRESS("amex", Pattern.compile("^3[47][0-9]{0,13}$")),
    ARGENCARD("argencard", Pattern.compile("^(50)(1)\\d*$")),
    BCMC("bcmc", Pattern.compile("^((6703)[0-9]{0,15}|(479658|606005)[0-9]{0,13})$")),
    BIJENKORF_CARD("bijcard", Pattern.compile("^(5100081)[0-9]{0,9}$")),
    CABAL("cabal", Pattern.compile("^(58|6[03])([03469])\\d*$")),
    CARTEBANCAIRE("cartebancaire", Pattern.compile("^[4-6][0-9]{0,15}$")),
    CODENSA("codensa", Pattern.compile("^(590712)[0-9]{0,10}$")),
    CUP("cup", Pattern.compile("^(62|81)[0-9]{0,17}$")),
    DANKORT("dankort", Pattern.compile("^(5019)[0-9]{0,12}$")),
    DINERS("diners", Pattern.compile("^(36)[0-9]{0,12}$")),
    DISCOVER("discover", Pattern.compile("^(6011[0-9]{0,12}|(644|645|646|647|648|649)[0-9]{0,13}|65[0-9]{0,14})$")),
    ELO("elo", Pattern.compile(
            "^((((506699)|(506770)|(506771)|(506772)|(506773)|(506774)|(506775)|(506776)|(506777)|(506778)|(401178)|(438935)|(451416)|(457631)|"
                    + "(457632)|(504175)|(627780)|(636368)|(636297))[0-9]{0,10})|((50676)|(50675)|(50674)|(50673)|(50672)|(50671)|(50670))[0-9]{0,"
                    + "11})$")),
    FORBRUGSFORENINGEN("forbrugsforeningen", Pattern.compile("^(60)(0)\\d*$")),
    VISAALPHABANKBONUS("visaalphabankbonus", Pattern.compile("^(450903)[0-9]{0,10}$")),
    MCALPHABANKBONUS("mcalphabankbonus", Pattern.compile("^(510099)[0-9]{0,10}$")),
    HIPER("hiper", Pattern.compile("^(637095|637599|637609|637612)[0-9]{0,10}$")),
    HIPERCARD("hipercard", Pattern.compile("^(606282)[0-9]{0,10}$")),
    JCB("jcb", Pattern.compile("^(352[8,9]{1}[0-9]{0,15}|35[4-8]{1}[0-9]{0,16})$")),
    OASIS("oasis", Pattern.compile("^(982616)[0-9]{0,10}$")),
    KARENMILLER("karenmillen", Pattern.compile("^(98261465)[0-9]{0,8}$")),
    WAREHOUSE("warehouse", Pattern.compile("^(982633)[0-9]{0,10}$")),
    LASER("laser", Pattern.compile("^(6304|6706|6709|6771)[0-9]{0,15}$")),
    MAESTRO("maestro", Pattern.compile("^(5[0|6-8][0-9]{0,17}|6[0-9]{0,18})$")),
    MAESTRO_UK("maestrouk", Pattern.compile("^(6759)[0-9]{0,15}$")),
    MASTERCARD("mc", Pattern.compile("^(5[1-5][0-9]{0,14}|2[2-7][0-9]{0,14})$")),
    MIR("mir", Pattern.compile("^(220)[0-9]{0,16}$")),
    NARANJA("naranja", Pattern.compile("^(37|40|5[28])([279])\\d*$")),
    SHOPPING("shopping", Pattern.compile("^(27|58|60)([39])\\d*$")),
    SOLO("solo", Pattern.compile("^(6767)[0-9]{0,15}$")),
    TROY("troy", Pattern.compile("^(97)(9)\\d*$")),
    UATP("uatp", Pattern.compile("^1[0-9]{0,14}$")),
    VISA("visa", Pattern.compile("^4[0-9]{0,18}$")),
    VISADANKORT("visadankort", Pattern.compile("^(4571)[0-9]{0,12}$"));

    private final String mTxVariant;

    private final Pattern mPattern;

    private static final Map<String, CardType> MAPPED_BY_NAME;

    static {
        final Map<String, CardType> hashMap = new HashMap<>();
        for (CardType type : CardType.values()) {
            hashMap.put(type.mTxVariant, type);
        }
        MAPPED_BY_NAME = Collections.unmodifiableMap(hashMap);
    }

    /**
     * Estimate all potential {@link CardType CardTypes} for a given card number.
     *
     * @param cardNumber The potential card number.
     * @return All matching {@link CardType CardTypes} if the number was valid, otherwise an empty {@link List}.
     */
    @NonNull
    public static List<CardType> estimate(@NonNull String cardNumber) {
        final List<CardType> result = new ArrayList<>();

        for (CardType type : CardType.values()) {
            if (type.isEstimateFor(cardNumber)) {
                result.add(type);
            }
        }

        return result;
    }

    /**
     * Get CardType from txVariant.
     */
    @Nullable
    public static CardType getCardTypeByTxVariant(@NonNull String txVariant) {
        return MAPPED_BY_NAME.get(txVariant);
    }


    CardType(@NonNull String txVariant, @NonNull Pattern pattern) {
        mTxVariant = txVariant;
        mPattern = pattern;
    }

    @NonNull
    public String getTxVariant() {
        return mTxVariant;
    }

    /**
     * Returns whether a given card number is estimated for this {@link CardType}.
     *
     * @param cardNumber The card number to make an estimation for.
     * @return Whether the {@link CardType} is an estimation for a given card number.
     */
    public boolean isEstimateFor(@NonNull String cardNumber) {
        final String normalizedCardNumber = cardNumber.replaceAll("\\s", "");
        final Matcher matcher = mPattern.matcher(normalizedCardNumber);

        return matcher.matches() || matcher.hitEnd();
    }
}
