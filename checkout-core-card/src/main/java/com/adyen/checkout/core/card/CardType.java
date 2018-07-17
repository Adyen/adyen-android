package com.adyen.checkout.core.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.TxVariantProvider;
import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.JsonObject.SerializedName;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/02/2018.
 */
public enum CardType implements TxVariantProvider {
    // NAME("txVariant", "^(first.two)(third)d*$"))
    ACCEL("accel", Pattern.compile("^(19|23|4\\d|5\\d|6[02348]|81)(\\d)\\d*$")),
    AFFN("affn", Pattern.compile("^(4\\d|5[0-8]|6[04])(\\d)\\d*$")),
    @SerializedName("amex")
    AMERICAN_EXPRESS("amex", Pattern.compile("^(3[47])(\\d)\\d*$")),
    ARGENCARD("argencard", Pattern.compile("^(50)(1)\\d*$")),
    BCMC("bcmc", Pattern.compile("^([46]7)([09])\\d*$")),
    @SerializedName("bijcard")
    BIJENKORF_CARD("bijcard", Pattern.compile("^(51)(0)\\d*$")),
    CABAL("cabal", Pattern.compile("^(58|6[03])([03469])\\d*$")),
    CADOCARTE("cadocarte", Pattern.compile("^(48)(9)\\d*$")),
    CARNET("carnet", Pattern.compile("^(28|5[08]|6[023])([246-9])\\d*$")),
    CARTEBANCAIRE("cartebancaire", Pattern.compile("^(18|3[07]|4\\d|5[0-68]|6[2379]|92)(\\d)\\d*$")),
    CENCOSUD("cencosud", Pattern.compile("^(60)(\\d)\\d*$")),
    CHEQUEDEJENEUR("chequedejeneur", Pattern.compile("^(54)(0)\\d*$")),
    CU24("cu24", Pattern.compile("^(4\\d|5[0-58]|6[34])(\\d)\\d*$")),
    CUP("cup", Pattern.compile("^(3[5-7]|4\\d|5[1-5]|6[02389]|9[0145689])(\\d)\\d*$")),
    DANKORT("dankort", Pattern.compile("^(50)([17])\\d*$")),
    DINERS("diners", Pattern.compile("^(36|65)(\\d)\\d*$")),
    DISCOVER("discover", Pattern.compile("^(36|6[045])(\\d)\\d*$")),
    EFTPOS_AUSTRALIA("eftpos_australia", Pattern.compile("^(4\\d|5[0-8]|6[023])(\\d)\\d*$")),
    ELO("elo", Pattern.compile("^(4[035]|50|6[235])([014-9])\\d*$")),
    FORBRUGSFORENINGEN("forbrugsforeningen", Pattern.compile("^(60)(0)\\d*$")),
    GOLDSMITHCARD("goldsmithscard", Pattern.compile("^(98)(2)\\d*$")),
    GOOGLEWALLET("googlewallet", Pattern.compile("^(51|60)([18])\\d*$")),
    HALLMARKCARD("hallmarkcard", Pattern.compile("^(98)(2)\\d*$")),
    HIPER("hiper", Pattern.compile("^(63)(7)\\d*$")),
    HIPERCARD("hipercard", Pattern.compile("^(38|60)([46])\\d*$")),
    INTERLINK("interlink", Pattern.compile("^(21|4\\d|5[0-8]|6[0-4]|92)(\\d)\\d*$")),
    JCB("jcb", Pattern.compile("^(35)([2-8])\\d*$")),
    LASER("laser", Pattern.compile("^(6[37])([07])\\d*$")),
    MAESTRO("maestro", Pattern.compile("^(21|4\\d|5[0-8]|6[0-47]|70|8[24])(\\d)\\d*$")),
    @SerializedName("maestrouk")
    MAESTRO_UK("maestrouk", Pattern.compile("^(6[37])([0135679])\\d*$")),
    MAPPINWEBBCARD("mappinwebbcard", Pattern.compile("^(98)(2)\\d*$")),
    @SerializedName("mc")
    MASTERCARD("mc", Pattern.compile("^(13|2[0-37]|35|4\\d|5[0-8]|6[0-478]|7[01357]|87|9[178])(\\d)\\d*$")),
    MIR("mir", Pattern.compile("^(22)(0)\\d*$")),
    NARANJA("naranja", Pattern.compile("^(37|40|5[28])([279])\\d*$")),
    NETPLUS("netplus", Pattern.compile("^(5[24])([18])\\d*$")),
    NETS("nets", Pattern.compile("^(11|88|99)([189])\\d*$")),
    NYCE("nyce", Pattern.compile("^(19|2[27]|4\\d|5[0-8]|6[02-5])(\\d)\\d*$")),
    PULSE("pulse", Pattern.compile("^(1[01]|22|3[016]|4\\d|5\\d|6[0-589]|81|9[0145689])(\\d)\\d*$")),
    SHAZAM("shazam", Pattern.compile("^(13|4\\d|5[0-8]|6[024])(\\d)\\d*$")),
    SHOPPING("shopping", Pattern.compile("^(27|58|60)([39])\\d*$")),
    SOLO("solo", Pattern.compile("^(67)(6)\\d*$")),
    STAR("star", Pattern.compile("^(4\\d|5[0-8]|6[0-5]|70)(\\d)\\d*$")),
    TROY("troy", Pattern.compile("^(97)(9)\\d*$")),
    UATP("uatp", Pattern.compile("^(1\\d)([0-8])\\d*$")),
    VISA("visa", Pattern.compile("^(4\\d|5[0678]|6[024]|7[034]|97)(\\d)\\d*$")),
    VISADANKORT("visadankort", Pattern.compile("^(45)(7)\\d*$")),
    VISASARAIVACARD("visasaraivacard", Pattern.compile("^(44)(4)\\d*$")),
    WOSCARD("woscard", Pattern.compile("^(98)(2)\\d*$"));

    private final String mTxVariant;

    private final Pattern mPattern;

    /**
     * Get the {@link CardType} for a given {@link TxVariantProvider}.
     *
     * @param txVariantProvider A {@link TxVariantProvider}.
     * @return The {@link CardType}, or {@code null} if no {@link CardType} matches the payment method type.
     */
    @Nullable
    public static CardType forTxVariantProvider(@NonNull TxVariantProvider txVariantProvider) {
        try {
            return JsonObject.parseEnumValue(txVariantProvider.getTxVariant(), CardType.class);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Estimate all potential {@link CardType CardTypes} for a given card number.
     *
     * @param cardNumber The potential card number.
     * @return All matching {@link CardType CardTypes} if the number was valid, otherwise an empty {@link List}.
     */
    @NonNull
    public static List<CardType> estimate(@NonNull String cardNumber) {
        List<CardType> result = new ArrayList<>();

        for (CardType type : CardType.values()) {
            if (type.isEstimateFor(cardNumber)) {
                result.add(type);
            }
        }

        return result;
    }

    CardType(@NonNull String txVariant, @NonNull Pattern pattern) {
        mTxVariant = txVariant;
        mPattern = pattern;
    }

    @NonNull
    @Override
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
        String normalizedCardNumber = cardNumber.replaceAll("\\s", "");
        Matcher matcher = mPattern.matcher(normalizedCardNumber);

        return (matcher.matches() || matcher.hitEnd()) && normalizedCardNumber.length() <= CardValidator.NUMBER_MAXIMUM_LENGTH;
    }
}
