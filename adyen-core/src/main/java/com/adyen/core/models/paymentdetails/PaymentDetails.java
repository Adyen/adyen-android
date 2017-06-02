package com.adyen.core.models.paymentdetails;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for saving payment details collected from user and to submit them to the sdk.
 * Different PaymentMethods will have different implementation of this class.
 */
public class PaymentDetails implements Serializable {

    Map<String, Object> map = new HashMap<>();

    /**
     * Construct a generic instance of {@link PaymentDetails} class by using a Map<String, Object> object.
     * @param inputMap The map containing the required fields and the payment details as key-value pairs.
     * @return An instance of {@link PaymentDetails}.
     */
    public static PaymentDetails fromMap(final Map<String, Object> inputMap) {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.map = new HashMap<>(inputMap);
        return paymentDetails;
    }

    /**
     * PaymentDetails is an abstraction of key-value pairs.
     * Therefore an PaymentDetails implements a toMap() method to retrieve the key value pairs.
     * @return The map containing these payment details.
     */
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(map);
    }
}
