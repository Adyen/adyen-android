/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 27/10/2017.
 */

package com.adyen.checkout.util.sepadirectdebit;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Ibans {
    public static final List<String> NETHERLANDS = Collections.unmodifiableList(netherlands());

    public static final List<String> GERMANY = Collections.unmodifiableList(germany());

    public static final List<String> ITALY = Collections.unmodifiableList(italy());

    public static final List<String> FRANCE = Collections.unmodifiableList(france());

    public static final List<String> SPAIN = Collections.unmodifiableList(spain());

    public static final List<String> AUSTRIA = Collections.unmodifiableList(austria());

    public static final List<String> SWITZERLAND = Collections.unmodifiableList(switzerland());

    public static final List<String> DENMARK = Collections.unmodifiableList(denmark());

    public static final List<String> GREAT_BRITAIN = Collections.unmodifiableList(greatBritain());

    public static final List<String> NORWAY = Collections.unmodifiableList(norway());

    public static final List<String> POLAND = Collections.unmodifiableList(poland());

    public static final List<String> SWEDEN = Collections.unmodifiableList(sweden());

    public static final List<List<String>> ALL = Collections.unmodifiableList(new ArrayList<List<String>>() {{
        add(NETHERLANDS);
        add(GERMANY);
        add(ITALY);
        add(FRANCE);
        add(SPAIN);
        add(AUSTRIA);
        add(SWITZERLAND);
        add(DENMARK);
        add(GREAT_BRITAIN);
        add(NORWAY);
        add(POLAND);
        add(SWEDEN);
    }});

    @NonNull
    private static List<String> netherlands() {
        return new ArrayList<String>() {{
            add("NL13 TEST 0123 4567 89");
            add("NL36 TEST 0236 1691 14");
            add("NL26 TEST 0336 1691 16");
            add("NL16 TEST 0436 1691 18");
            add("NL81 TEST 0536 1691 28");
            add("NL27 TEST 0636 1691 46");
            add("NL39 TEST 0736 1692 37");
            add("NL82 TEST 0836 1692 55");
            add("NL72 TEST 0936 1692 57");
            add("NL46 TEST 0136 1691 12");
            add("NL70 TEST 0736 1603 37");
            add("NL18 TEST 0736 1624 37");
            add("NL92 TEST 0736 1634 33");
        }};
    }

    @NonNull
    private static List<String> germany() {
        return new ArrayList<String>() {{
            add("DE87 1234 5678 1234 5678 90");
            add("DE92 1234 5678 9876 5432 10");
            add("DE14 1234 5678 0023 4567 89");
            add("DE36 4444 8888 1234 5678 90");
            add("DE41 4444 8888 9876 5432 10");
            add("DE60 4444 8888 0023 4567 89");
            add("DE89 8888 8888 1234 5678 90");
            add("DE94 8888 8888 9876 5432 10");
            add("DE16 8888 8888 0023 4567 89");
        }};
    }

    @NonNull
    private static List<String> italy() {
        return new ArrayList<String>() {{
            add("IT60 X054 2811 1010 0000 0123 456");
        }};
    }

    @NonNull
    private static List<String> france() {
        return new ArrayList<String>() {{
            add("FR14 2004 1010 0505 0001 3M02 606");
        }};
    }

    @NonNull
    private static List<String> spain() {
        return new ArrayList<String>() {{
            add("ES91 2100 0418 4502 0005 1332");
        }};
    }

    @NonNull
    private static List<String> austria() {
        return new ArrayList<String>() {{
            add("AT15 1234 5123 4567 8901");
        }};
    }

    @NonNull
    private static List<String> switzerland() {
        return new ArrayList<String>() {{
            add("CH49 1234 5123 4567 8901 2");
        }};
    }

    @NonNull
    private static List<String> denmark() {
        return new ArrayList<String>() {{
            add("DK86 1234 1234 5678 90");
        }};
    }

    @NonNull
    private static List<String> greatBritain() {
        return new ArrayList<String>() {{
            add("GB85 TEST 1234 5612 3456 78");
        }};
    }

    @NonNull
    private static List<String> norway() {
        return new ArrayList<String>() {{
            add("NO60 1234 1234 561");
        }};
    }

    @NonNull
    private static List<String> poland() {
        return new ArrayList<String>() {{
            add("PL20 1231 2341 1234 5678 9012 3456");
        }};
    }

    @NonNull
    private static List<String> sweden() {
        return new ArrayList<String>() {{
            add("SE94 1231 2345 6789 0123 4561");
        }};
    }

    private Ibans() {
        throw new IllegalStateException("No instances.");
    }
}
