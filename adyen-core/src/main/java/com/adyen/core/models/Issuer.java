package com.adyen.core.models;

import org.json.JSONObject;

import java.io.Serializable;
import java.net.URL;

/**
 * A class to store issuer information.
 */

public class Issuer implements Serializable {

    private static final long serialVersionUID = -7308915381972392922L;
    private String issuerId;
    private String issuerLogoUrl;
    private String issuerName;

    Issuer(final JSONObject issuerJSONObject) {
        issuerId = issuerJSONObject.optString("id");
        issuerLogoUrl = issuerJSONObject.optString("imageUrl");
        issuerName = issuerJSONObject.optString("name");
    }

    /**
     * Get the issuer logo URL.
     * @return The issuer logo URL as a {@link URL} object.
     */
    public String getIssuerLogoUrl() {
        return issuerLogoUrl;
    }

    /**
     * Get the issuer name.
     * @return The issuer name.
     */
    public String getIssuerName() {
        return issuerName;
    }

    /**
     * Get the issuer ID.
     * @return The issuer ID in a string format.
     */
    public String getIssuerId() {
        return issuerId;
    }

}
