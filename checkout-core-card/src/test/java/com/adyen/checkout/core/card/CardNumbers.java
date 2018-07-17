package com.adyen.checkout.core.card;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 27/10/2017.
 */
public final class CardNumbers {
    public static final List<String> MASTERCARD;

    public static final List<String> VISA;

    public static final List<String> JCB;

    public static final List<String> CARTEBANCAIRE;

    public static final List<String> AMERICAN_EXPRESS;

    public static final List<String> DINERS;

    public static final List<String> DISCOVER;

    public static final List<String> BANCONTACT_BCMC;

    public static final List<String> HIPERCARD;

    public static final List<String> ELO;

    public static final List<String> DANKORT;

    public static final List<String> UNIONPAY_SECUREPLUS_DEBIT;

    public static final List<String> UNIONPAY_SECUREPLUS_CREDIT;

    public static final List<String> UNIONPAY_SECUREPAY_DEBIT;

    public static final List<String> UNIONPAY_SECUREPAY_CREDIT;

    public static final List<String> UNIONPAY_EXPRESSPAY_CREDIT;

    public static final List<String> UATP;

    public static final Map<CardType, List<String>> TYPE_NUMBERS_MAPPING;

    static {
        try {
            MASTERCARD = Collections.unmodifiableList(mastercard());
            VISA = Collections.unmodifiableList(visa());
            JCB = Collections.unmodifiableList(jcb());
            CARTEBANCAIRE = Collections.unmodifiableList(cartebaincaire());
            AMERICAN_EXPRESS = Collections.unmodifiableList(americanExpress());
            DINERS = Collections.unmodifiableList(diners());
            DISCOVER = Collections.unmodifiableList(discover());
            BANCONTACT_BCMC = Collections.unmodifiableList(bancontactBcmc());
            HIPERCARD = Collections.unmodifiableList(hipercard());
            ELO = Collections.unmodifiableList(elo());
            DANKORT = Collections.unmodifiableList(dankort());
            UNIONPAY_SECUREPLUS_DEBIT = Collections.unmodifiableList(unionpaySecureplusDebit());
            UNIONPAY_SECUREPLUS_CREDIT = Collections.unmodifiableList(unionpaySecureplusCredit());
            UNIONPAY_SECUREPAY_DEBIT = Collections.unmodifiableList(unionpaySecurepayDebit());
            UNIONPAY_SECUREPAY_CREDIT = Collections.unmodifiableList(unionpaySecurepayCredit());
            UNIONPAY_EXPRESSPAY_CREDIT = Collections.unmodifiableList(unionpayExpresspayCredit());
            UATP = Collections.unmodifiableList(uatp());
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing credit card number.", e);
        }

        TYPE_NUMBERS_MAPPING = Collections.unmodifiableMap(typeNumbersMapping());
    }

    @NonNull
    private static List<String> mastercard() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("2223 0000 4841 0010"));
            add(assertIsValidNumber("2223 5204 4356 0010"));
            add(assertIsValidNumber("2222 4107 4036 0010"));
            add(assertIsValidNumber("5100 0811 1222 3332"));
            add(assertIsValidNumber("5103 2219 1119 9245"));
            add(assertIsValidNumber("5100 2900 2900 2909"));
            add(assertIsValidNumber("5577 0000 5577 0004"));
            add(assertIsValidNumber("5136 3333 3333 3335"));
            add(assertIsValidNumber("5585 5585 5585 5583"));
            add(assertIsValidNumber("5555 4444 3333 1111"));
            add(assertIsValidNumber("5555 5555 5555 4444"));
            add(assertIsValidNumber("5500 0000 0000 0004"));
            add(assertIsValidNumber("5424 0000 0000 0015"));
        }};
    }

    @NonNull
    private static List<String> visa() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("4111 1111 1111 1111"));
            add(assertIsValidNumber("4988 4388 4388 4305"));
            add(assertIsValidNumber("4166 6766 6766 6746"));
            add(assertIsValidNumber("4646 4646 4646 4644"));
            add(assertIsValidNumber("4444 3333 2222 1111"));
            add(assertIsValidNumber("4400 0000 0000 0008"));
            add(assertIsValidNumber("4977 9494 9494 9497"));
        }};
    }

    @NonNull
    private static List<String> jcb() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("3569 9900 1009 5841"));
        }};
    }

    @NonNull
    private static List<String> cartebaincaire() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("4035 5010 0000 0008"));
            add(assertIsValidNumber("4360 0000 0100 0005"));
        }};
    }

    @NonNull
    private static List<String> americanExpress() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("3700 000000 00002"));
            add(assertIsValidNumber("3710 000000 00001"));
        }};
    }

    @NonNull
    private static List<String> diners() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("3600 6666 3333 44"));
        }};
    }

    @NonNull
    private static List<String> discover() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6011 6011 6011 6611"));
            add(assertIsValidNumber("6445 6445 6445 6445"));
        }};
    }

    @NonNull
    private static List<String> bancontactBcmc() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6703 4444 4444 4449"));
        }};
    }

    @NonNull
    private static List<String> hipercard() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6062 8288 8866 6688"));
        }};
    }


    @NonNull
    private static List<String> elo() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("5066 9911 1111 1118"));
        }};
    }

    @NonNull
    private static List<String> dankort() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("5019 5555 4444 5555"));
        }};
    }

    @NonNull
    private static List<String> unionpaySecureplusDebit() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6250 9460 0000 0016"));
        }};
    }

    @NonNull
    private static List<String> unionpaySecureplusCredit() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6250 9470 0000 0014"));
        }};
    }

    @NonNull
    private static List<String> unionpaySecurepayDebit() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6250 9460 0000 0016"));
        }};
    }

    @NonNull
    private static List<String> unionpaySecurepayCredit() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6250 9470 0000 0014"));
        }};
    }

    @NonNull
    private static List<String> unionpayExpresspayCredit() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("6243 0300 0000 0001"));
        }};
    }

    @NonNull
    private static List<String> uatp() throws ParseException {
        return new ArrayList<String>() {{
            add(assertIsValidNumber("1354 1001 4004 955"));
        }};
    }

    @NonNull
    private static Map<CardType, List<String>> typeNumbersMapping() {
        return new HashMap<CardType, List<String>>() {{
            put(CardType.MASTERCARD, MASTERCARD);
            put(CardType.VISA, VISA);
            put(CardType.JCB, JCB);
            put(CardType.CARTEBANCAIRE, CARTEBANCAIRE);
            put(CardType.AMERICAN_EXPRESS, AMERICAN_EXPRESS);
            put(CardType.DINERS, DINERS);
            put(CardType.DISCOVER, DISCOVER);
            put(CardType.BCMC, BANCONTACT_BCMC);
            put(CardType.HIPERCARD, HIPERCARD);
            put(CardType.ELO, ELO);
            put(CardType.DANKORT, DANKORT);
            put(CardType.CUP, allUnionPay());
            put(CardType.UATP, UATP);
        }};
    }

    @NonNull
    private static List<String> allUnionPay() {
        List<String> result = new ArrayList<>();

        result.addAll(UNIONPAY_SECUREPLUS_DEBIT);
        result.addAll(UNIONPAY_SECUREPLUS_CREDIT);
        result.addAll(UNIONPAY_SECUREPAY_DEBIT);
        result.addAll(UNIONPAY_SECUREPAY_CREDIT);
        result.addAll(UNIONPAY_EXPRESSPAY_CREDIT);

        return result;
    }

    @NonNull
    private static String assertIsValidNumber(@NonNull String number) {
        CardValidator.NumberValidationResult validationResult = Cards.VALIDATOR.validateNumber(number);

        if (validationResult.getValidity() == CardValidator.Validity.VALID) {
            String result = validationResult.getNumber();
            assertNotNull(result);

            return result;
        } else {
            throw new IllegalStateException("Number is invalid.");
        }
    }

    private CardNumbers() {
        throw new IllegalStateException("No instances.");
    }
}
