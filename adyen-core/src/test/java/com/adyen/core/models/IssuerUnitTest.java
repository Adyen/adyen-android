package com.adyen.core.models;

import com.adyen.core.utils.Util;

import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Issuer} class.
 */

public class IssuerUnitTest {
    private final static String TEST_ISSUER_NAME = "Test Issuer";

    @Test
    public void testIssuer() throws Exception {
        final Issuer issuer = new Issuer(getJSONFromFile("issuer.json"));
        assertEquals("Issuer id could not be set correctly", "1121", issuer.getIssuerId());
        assertEquals("Issuer name could not be set correctly", TEST_ISSUER_NAME, issuer.getIssuerName());
        assertEquals("Issuer logo could not be set correctly", "/TESTBANK.png", issuer.getIssuerLogoUrl());
    }

    // TODO: The following method has been copy pasted in several classes; move to a generic TestUtils class.
    private JSONObject getJSONFromFile(final String fileName) throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(fileName).getFile());
        byte[] jsonInput = Util.convertInputStreamToByteArray(new FileInputStream(file));
        return new JSONObject(new String(jsonInput));
    }
}
