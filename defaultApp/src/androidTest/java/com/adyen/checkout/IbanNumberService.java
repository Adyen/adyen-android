package com.adyen.checkout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IbanNumberService {

    public static Collection<String> getIbanNumbers() {
        List<String> list = new ArrayList<>();
        list.add("NL13TEST0123456789");
        list.add("NL36TEST0236169114");
        list.add("NL26TEST0336169116");
        list.add("NL16TEST0436169118");
        list.add("NL81TEST0536169128");
        list.add("NL27TEST0636169146");
        list.add("NL39TEST0736169237");
        list.add("NL82TEST0836169255");
        list.add("NL72TEST0936169257");
        list.add("NL46TEST0136169112");
        list.add("NL70TEST0736160337");
        list.add("NL18TEST0736162437");
        list.add("NL92TEST0736163433");
        list.add("DE87123456781234567890");
        list.add("DE92123456789876543210");
        list.add("DE14123456780023456789");
        list.add("DE36444488881234567890");
        list.add("DE41444488889876543210");
        list.add("DE60444488880023456789");
        list.add("DE89888888881234567890");
        list.add("DE94888888889876543210");
        list.add("DE16888888880023456789");
        list.add("IT60X0542811101000000123456");
        list.add("FR1420041010050500013M02606");
        list.add("ES9121000418450200051332");
        list.add("AT151234512345678901");
        list.add("CH4912345123456789012");
        list.add("DK8612341234567890");
        list.add("GB85TEST12345612345678");
        list.add("NO6012341234561");
        list.add("PL20123123411234567890123456");
        list.add("SE9412312345678901234561");
        return list;
    }

    private IbanNumberService() {
        // Private constructor
    }
}
