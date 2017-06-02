package com.adyen.core.utils;

import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.PaymentResponse;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Unit tests for PaymentResponse class.
 */

public class PaymentResponseUnitTest {

    private static final String PUBLIC_KEY = "10001|A5C3948EA33240F035A6E684F588783E90CD878D6951B0310922E7DCB2C56AC565"
            + "9B9C4B99AE7C755847134AE71AED04FE97CE73C4422241EA2F268D26A40D30941780476664B91A0B70F1F8D3CF58608FBF8FECD"
            + "1EB97FE6A9AB596C66DFDA910380087F71A05BC7E092B77BF49FB22D0BD2CB948790E3B3550C3B8C4322BBC7FA242F7FCF048A6"
            + "2A266AA674643926B6FBD1BBF5C912E8D28E2FC6D4AF7BF1793534174707F35F8AC728574E028A6867558F8C677AD4F1FDB9548"
            + "DDA37B18BB63510EBFC102901B3B8700A28AB78CF52073713C5765B932CA96C2A5E3A67483E573FBBDB97F42909BD0777E99F5C"
            + "421F6872B272380E54EC2F1C0C29C6067B";
    private static final String PAYMENT_DATA = "BB8329KFFnn942rr!ZW5jcnlwdGVkPXpvVTZMVTFDdVZObmNaMkJkMER2Uk5mbm1WJTJGU"
            + "kdLdVpURmQ4cW1tUW5lWGlSbEZyVzFwM0ZkVTFhQUVVSUxEcnZvNUQ2Q3JIeFFXMFJUVVFVNmNEJTJGRmFrRXRYVENFayUyRmglMkI5"
            + "N0RXcnltM3psWkdaZmk3ZHl0RCUyRmdqRCUyRnpJeHdSJTJCQVA1WE82aWhoblhCcWQ0bTVZSktVNXRid1hkVEkyVzhieU9qN3EzZXp"
            + "mVDY2ZmdjTnJWaXFxTzBWSGF3bklKbFpaM3pqdWRoUzlsUGtPTUlDMmdyeVVPYXZlVVo3bEtwZSUyQndadDJMaWN1NiUyRnY2WURsJT"
            + "JCc2JGOHdLM3NhNkVub0V3OGQzc1MlMkZRMzU3VTladGEyVmduJTJGTW0lMkYwSmt3JTJGc1pqakhoWktobDVKTXlvQmt5RkElMkZQa"
            + "2ZyWU1kT0RqV0tUM2lSeE1BbHF6MVBmR0lTWnhMOVFXd05jajV1SXlCQ1pRMWFzRW9CbXM2QjVKOE1tenV1OGU4SlBnYUlsQ0VEalQl"
            + "MkZCcmlJZHVPc1hGRyUyQlNTQ3g0akxkUlN3QkxMWjZ2Z3N1SHFVZlZpaTBVQXN4V1pITjMxU0hQRU9jeFUxMlgycWJyaTVEVlZqMFd"
            + "QSyUyQlVhVURlZ2JYblFMZjBhVzk4NkE5M3Vhcm1abjc1bHcwaHRTJTJGSVZ3d1gzV203WDhYc0wzeiUyQnFPQlpBJTJGVHFJNUtyWn"
            + "ZmUWtIcHBnSENBRzAyUXBIUiUyQjZ0UmY5RWRpeTlEdm4zcXhvOWh6WEdleWtmak9CS214dkpZclprdkZJT0hRcnAlMkIya3lKSE5rd"
            + "XlQNjNublhtOXl3MWVQOXFLSGRZd0lTaSUyRmRhOHc2R0VPdkdhYWg2ZnlhTDU4b1F0dUpVUVdFS1RZOW00YmtqNnlwTXU4WG95b2RR"
            + "VkZaQzJlcVROcTRUa0clMkZDRUVPZVVIUXlsdFVKc1ZWemkwT0lIJTJCb2RXWmhHbWR0SmczcU9BOWZ5S0N4eGc4a3dFZ3Fyb1RCYTF"
            + "5cTlwa1RXcW1sR1clMkJrcFI2dlhkZTdNejVja3R1R3RqMnJaNmdHQW85UlF5S1hkOWRlckElMkJXJTJCUm1ZYTF2czZFRGlzSU54aH"
            + "JhOW9IY3dBMm1SQjdnZFNqMEhZbFJYd01DREl0QzVhZk5sS1kwc3paJTJGc0Y4THV1d1dJQmtvb0hGamRKeVpKbU9SalROS3FUdFpsR"
            + "EJvY0t2c1hLVmttOGN4d1VNTFZMVlptVmFPcURiNTA3bkpWQzklMkJuR0Exbm9OSjU4S3BFYkMya2pMeXRiVDMyJTJCa3UlMkZHUG9h"
            + "eW9VRHdMZlZJRlY1S3Q1SW1rc0JJUnZMJTJGSGxGVDVDekNMSjJ4YXY5eGVYWTElMkJBcTJlTFVwZFFvYXc3djlTcXRPSTRkNm04V2N"
            + "VRUJjT25CSXY1QzVnVldKcTFZRGthaEtsZTVBaXRvOWF3RklSdnlnSiUyQjhEJTJCWEhOVCUyRjJnVzVqTzM4dSUyQllyYWwlMkJIc2"
            + "5La1lNQnpKdzRUMGZmOTJTWjVNSEx6SjFTRWh1UWdIRk5rcVRmSGtKSHQ2YkZkTlcwVnNyaVFvWG1DVEVvTWRaVWk2T082V2hZRmolM"
            + "kZrNGNmdGVEN1RHRm45UWN3cGRXZzAwazZsdjBIZ3FTVUU2YlhGZWw3RTFBRGdiTG4weEN4NUxVZmM0alhxS3dXeSUyQm5Sdk5JbE84"
            + "M3ZtdG93N2ZZMnNyT0oxamFmTnRYMklYMkZPRDZzME5za1BlMEU5NHdKM1M1YU5iR3ZBRmpKWXNzS1RPQTBCb3ZXVCUyRkM5STZrJTJ"
            + "CSlYwWnBtRktpejVvQWtKWWVLYjJuQXhkTE1YcDM2eFNWbDVqZ2psQUlqTjdWTUNFZk9ONGRIYkElMkJDMVJING1aejZCcDAwVHZIWE"
            + "J0MzNHVnZQNjAxZlVQVm1ERzcyMnlSN01tSXFvNmplM1FVeklVdTNGZlRvTVN6UGREQ0tZTmFrUWlwZm9HTmpkQlA1TkJBR1NyUkdjb"
            + "jdOU0RKNyUyRjJEQ0pOeFNxeTcycUZRR2hMQVQ4eURjZDN2JTJCUCUyQnN3aHpKRFRPeXI5ZXA0MFdlclFocEVwaiUyRjJtWVlnSWF1"
            + "bVVCN09DMk92SHlQR1RIaDBod3IlMkI3UjhiaTZNTDQ1WGpPcVl4b3FUaE9RRFBxcXRtdUZBRWs5S1Q1SVphJTJGOVFkektJQ0o5eEl"
            + "3Q3RrSnVQcHFjVjJLUmkzZVhTelZ5SUhsdG5ZYVRnJTJCNTFNUXZuZ2ZaZ2ElMkZKTHQ5ZlRMOWExdnZjN0wlMkZicGdqYVhPc3NRUk"
            + "p4Z3loTHV6cjFGT21hd1hNVjYlMkJUc2YlMkJwTzNDNXc2ckZDTGx6cGZSZnZLMlZmUUxtRFRRRTU4UlNXM1F3eVZHeEJPZ0RBY1ZNS"
            + "Wt0a0RnTVFpZE85RjN0T0EzY1NUNTl0U0lqN0lHR3dIM0hBb2Z6JTJGZ0phMjZqc055bUNOWWJ1aWRzQkRKVFprY1QxSG1pZmxOY2F4"
            + "bGFjRG0lMkJINGlNSFQwN0tsQU53QmE4YWJwa21nVHlvTEdEd2VkRTlEdUdVdjV1ejlhanpXUm9jZTdoU2VEa3lwNWJlV0wlMkZkdk1"
            + "0WURMQ0tCdWxRZ2plRHQwQmx2SEc2WUpQc1VaeHlvb3hFV0JUZU1PJTJCaFZLMEswY2l1MENQVFNXWllLOGgwRFhjVW4ybEhYY1p0MU"
            + "JmOUs4RHUxaU1EUU9KY1RtOHdOeEFFVjQlMkZCY3A2Rk15azlQUXIwJTJGT3lVQkVhc25ScUZmS1QzNnZEM20xbzFjNmtDY2RpM1I5M"
            + "XBmdEVpMHMwNWtBMDJkdGN4WW03Mm5STHF3NGNoNG50UWM4VTRXNWc5WlgmZW5jcnlwdGVkS2V5PXN0VklVeWVxY3B3ZzNmc0R0aXpK"
            + "ZVdmZkhXU0gxVzl4WFpCMGFEbHY3SURzTHVDY2FIUG1GMGpGb25tRHhaVWFzbHJMMFB4YnJKTCUyQmhsNjdvMWc0amMlMkJ6cnJtYlZ"
            + "yRldMeWdicUIzRCUyQmRTSml4NkxHUGdwUEJIMlIlMkZoUHNWVkJEeU1CbSUyRnV4JTJCb2ZmcjFGbjd6c0d4M0toJTJCbmlJWXBJJT"
            + "JGU2pVTkVTdTc4NHV5VXNZcCUyQlFVZFZjZWg4UzdOcDc5MjVSdU9hUFprOUlCalhxVWFDU1hzUDlkUmJsRHYyeVMyU2ZaWnZhUkt0T"
            + "2hBZGxIN05JTDJoUVlqT1dZUnQlMkZ0JTJCZXp5M0s2cnBrYiUyQiUyRnBMZzBldlk1JTJGWnVObllQMEpwM3pyTUZBakEwNVFtU3g5"
            + "U2x5ZDdkVCUyRkE5dkU2RTBPa0dMMVNyQ1k5UDRKVUk5M2pIRXI1VGJIOSUyRmdhOTZzMUVUY3glMkZnc2hKbGhRdDlkQWNVYWVlQkF"
            + "YQ2xYS3loZ3I2WW5YcVVWNkcxdmltZ2wxSExNRE96RWR6VFZDYmJEN1p1Q29UJTJCenZDUWUwSk5FZ1Z6Wkw0dzUlMkZ0QUhUcW9RUU"
            + "tEZk1NeCUyQlI5N3BQN1AzNnJxMkd4ZXElMkJ1dzRoUzkwcjUlMkZialYxY1dnWWN5ZklnM3lQWUt2SFlCZzVuUUF1SHFSclNkTEZye"
            + "TJ0T2RpTm15JTJCTFJ4UU1ONFc3V2FHJTJCJTJCdyUyQkolMkY5N1VPczc4Z1dOZWo0T05tM3ZFNHJwc1Vrb3RlQnV4WklTVWh3Zjg1"
            + "UW9Vc3JiTTMyeSUyRiUyRnRNMmpGSjVlYUlXRk8yOWk0ZGJqTnVDSERFcmU5TkthMDVSWTklMkJlNmJNNk02U0drRlRBZXY4djB6T1J"
            + "IVWplbVpUUGdoTHdKcEZRenljZzRZckJjeXVnJTNEJmtleUlkPUFGMEFBQTEwM0NBNTM3RUFFRDg3QzI0REQ1MzkwOUI4MEE3OEE5Mj"
            + "NFMzgyM0Q2OERBQ0M5NEI5RkY4MzA1REMmaXY9bWRWa1lpUVJsQ1lzRkVPbHg1N25mQSUzRCUzRA==";

    private static final String GENERATION_TIME = "2017-02-28T15:48:54Z";
    private static final String INITIATION_URL = "https://shopper-beta.adyen.com/services/PaymentInitiation"
            + "/v1/initiate";
    private static final String LOGO_BASE_URL = "https://beta.adyen.com/hpp/img/pm/";
    private static final String REFERENCE = "M+M Black dress & accessories";
    private static final String SESSION_VALIDITY = "2017-05-05T13:09:50";
    private static final String COUNTRY_CODE = "NL";
    private static final String SHOPPER_REFERENCE = "emred";
    private static final String PUBLIC_KEY_TOKEN = "9314230528005578";
    private static final String ORIGIN = "";

    @Test
    public void testPublicKey() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(PUBLIC_KEY, paymentResponse.getPublicKey());
    }

    @Test
    public void testPaymentData() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(PAYMENT_DATA, paymentResponse.getPaymentData());
    }

    @Test
    public void testGenerationTime() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(GENERATION_TIME, paymentResponse.getGenerationTime());
    }

    @Test
    public void testInitiationURL() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(INITIATION_URL, paymentResponse.getInitiationURL());
    }

    @Test
    public void testLogoBaseURL() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(LOGO_BASE_URL, paymentResponse.getLogoBaseURL());
    }

    @Test
    public void testAmount() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(5000, paymentResponse.getAmount().getValue());
        assertEquals("EUR", paymentResponse.getAmount().getCurrency());
    }

    @Test
    public void testReference() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(REFERENCE, paymentResponse.getReference());
    }

    @Test
    public void testSessionValidity() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(SESSION_VALIDITY, paymentResponse.getSessionValidity());
    }

    @Test
    public void testPublicKeyToken() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(PUBLIC_KEY_TOKEN, paymentResponse.getPublicKeyToken());
    }

    @Test
    public void testCountryCode() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(COUNTRY_CODE, paymentResponse.getCountryCode());
    }

    @Test
    public void testShopperReference() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(SHOPPER_REFERENCE, paymentResponse.getShopperReference());
    }

    @Test
    public void testOrigin() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        assertEquals(ORIGIN, paymentResponse.getOrigin());
    }

    @Test
    public void testPaymentMethods() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        final List<PaymentMethod> paymentMethods = paymentResponse.getAvailablePaymentMethods();
        assertEquals(44, paymentMethods.size());
        for (final PaymentMethod paymentMethod : paymentMethods) {
            if ("ideal".equals(paymentMethod.getType())) {
                assertEquals(14, paymentMethod.getIssuers().size());
            }
        }
    }

    @Test
    public void testPreferredPaymentMethods() throws Exception {
        final PaymentResponse paymentResponse = getPaymentResponseFromFile("setup-response.json");
        final List<PaymentMethod> paymentMethods = paymentResponse.getPreferredPaymentMethods();
        assertEquals(3, paymentMethods.size());
    }

    private PaymentResponse getPaymentResponseFromFile(final String fileName) throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(fileName).getFile());
        byte[] jsonInput = Util.convertInputStreamToByteArray(new FileInputStream(file));
        return new PaymentResponse(jsonInput);
    }

}
