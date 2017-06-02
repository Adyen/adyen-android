package com.adyen.core.models;

import com.adyen.core.utils.Util;

import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static junit.framework.Assert.assertEquals;

public class PaymentMethodUnitTest {

    @Test
    public void testPaymentMethodName() throws Exception {
        final PaymentMethod paymentMethod = PaymentMethod.createPaymentMethod(
                getJSONFromFile("paymentMethod.json"), "http://dummy/");
        assertEquals("Payment method name was supposed to be empty", "de Bijenkorf Card", paymentMethod.getName());
    }

    @Test
    public void testPaymentMethodType() throws Exception {
        final PaymentMethod paymentMethod = PaymentMethod.createPaymentMethod(
                getJSONFromFile("paymentMethod.json"), "http://dummy/");
        assertEquals("Payment method type was supposed to be testMethodType", "bijcard", paymentMethod.getType());
    }

    @Test
    public void testPaymentMethodLogoURL() throws Exception {
        final PaymentMethod paymentMethod = PaymentMethod.createPaymentMethod(
                getJSONFromFile("paymentMethod.json"), "http://dummy/");
        assertEquals("Received payment method Logo URL: " + paymentMethod.getLogoUrl(), "http://dummy/bijcard.png",
                paymentMethod.getLogoUrl());
    }

    @Test
    public void testPaymentMethodData() throws Exception {
        final PaymentMethod paymentMethod = PaymentMethod.createPaymentMethod(
                getJSONFromFile("paymentMethod.json"), "http://dummy/");
        assertEquals("Received payment method data: " + paymentMethod.getPaymentMethodData(),
                "YUZXk7Ut8w8cCuNR!0101219366CF71DEBD344117BE68B9986739B5E01417E4113B651DB6C4"
                        + "3C05A0C644AE4510C15D5B0DBEE47CDCB5588C48224C6007", paymentMethod.getPaymentMethodData());
    }

    private JSONObject getJSONFromFile(final String fileName) throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(fileName).getFile());
        byte[] jsonInput = Util.convertInputStreamToByteArray(new FileInputStream(file));
        return new JSONObject(new String(jsonInput));
    }

}
