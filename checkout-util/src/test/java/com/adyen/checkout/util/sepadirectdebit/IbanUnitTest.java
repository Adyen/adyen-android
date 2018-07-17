package com.adyen.checkout.util.sepadirectdebit;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 27/10/2017.
 */
public class IbanUnitTest {
    @Test
    public void testIbanParsingAndFormatting() {
        for (List<String> ibanValues : Ibans.ALL) {
            for (String ibanValue : ibanValues) {
                System.out.println("Testing formatting/parsing of IBAN " + ibanValue);

                Iban iban = Iban.parse(ibanValue);
                assertTrue("IBAN could not be parsed: " + ibanValue, iban != null);

                String formatted = Iban.format(iban.getValue());
                assertTrue("IBAN is not correctly formatted.", ibanValue.equals(formatted));
            }
        }
    }
}
