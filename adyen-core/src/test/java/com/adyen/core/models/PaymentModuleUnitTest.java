package com.adyen.core.models;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Unit tests for {@link PaymentModule} class.
 */
public class PaymentModuleUnitTest {
    @Test
    public void testPaymentMethodIntegrity() throws Exception {
        final PaymentModule paymentModule = PaymentModule.samsungpay;
        assertEquals("Payment method service name is incorrect", "com.adyen.samsungpay.SamsungPayService",
                paymentModule.getServiceName());
    }
}
