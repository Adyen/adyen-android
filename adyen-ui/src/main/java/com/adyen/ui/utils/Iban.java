package com.adyen.ui.utils;

import com.adyen.core.utils.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;

/*
 * Source: https://www.swift.com/sites/default/files/resources/swift_standards_ibanregistry.pdf
 * Source: http://en.wikipedia.org/wiki/International_Bank_Account_Number
 */
public enum Iban {
    /* k: Check Digit (after the country code)
     * B: Bank Code
     * S: Branch Code
     * C: Account Number
     * R: Check Code
     * K: Check Digit (ignored)
     */

    AL_Albania("ALkkBBBSSSSRCCCCCCCCCCCCCCCC"),

    AD_Andorra("ADkkBBBBSSSSCCCCCCCCCCCC"),

    AT_Austria("ATkkBBBBBCCCCCCCCCCC"),

    AZ_Azerbaijan("AZkkBBBBCCCCCCCCCCCCCCCCCCCC"),

    BH_Bahrain("BHkkBBBBCCCCCCCCCCCCCC"),

    BE_Belgium("BEkkBBBCCCCCCCCC"),

    BA_Bosnia_and_herzegovina("BAkkBBBSSSCCCCCCCCRR"),

    BG_Bulgaria("BGkkBBBBSSSSCCCCCCCCCC"),

    CR_Costa_rica("CRkkBBBCCCCCCCCCCCCCC"),

    HR_Croatia("HRkkBBBBBBBCCCCCCCCCC"),

    CY_Cyprus("CYkkBBBSSSSSCCCCCCCCCCCCCCCC"),

    CZ_Czech_republic("CZkkBBBBSSSSSSCCCCCCCCCC"),

    DK_Denmark("DKkkBBBBCCCCCCCCCC"),

    DO_Dominican_republic("DOkkBBBBCCCCCCCCCCCCCCCCCCCC"),

    EE_Estonia("EEkkBBSSCCCCCCCCCCCR"),

    FO_Faroe_islands("FOkkBBBBCCCCCCCCCR"),

    FI_Finland("FIkkSSSSSSCCCCCCCR"),

    FR_France("FRkkBBBBBSSSSSCCCCCCCCCCCRR", "BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM",
            "RE", "TF", "WF", "YT"),

    GE_Georgia("GEkkBBCCCCCCCCCCCCCCCC"),

    DE_Germany("DEkkSSSSSSSSCCCCCCCCCC"),

    GI_Gibraltar("GIkkSSSSCCCCCCCCCCCCCCC"),

    GR_Greece("GRkkBBBSSSSCCCCCCCCCCCCCCCC"),

    GL_Greenland("GLkkBBBBCCCCCCCCCC"),

    GT_Guatemala("GTkkBBBBCCCCCCCCCCCCCCCCCCCC"),

    HU_Hungary("HUkkBBBSSSSRCCCCCCCCCCCCCCCR"),

    IS_Iceland("ISkkBBBBSSCCCCCCCCCCCCCCCC"),

    IE_Ireland("IEkkBBBBSSSSSSCCCCCCCC"),

    IL_Israel("ILkkBBBSSSCCCCCCCCCCCCC"),

    IT_Italy("ITkkRBBBBBSSSSSCCCCCCCCCCCC"),

    JO_Jordan("JOkkBBBBSSSSCCCCCCCCCCCCCCCCCC"),

    KZ_Kazakhstan("KZkkBBBCCCCCCCCCCCCC"),

    KW_Kuwait("KWkkBBBBCCCCCCCCCCCCCCCCCCCCCC"),

    LV_Latvia("LVkkBBBBCCCCCCCCCCCCC"),

    LB_Lebanon("LBkkBBBBCCCCCCCCCCCCCCCCCCCC"),

    LI_Liechtenstein("LIkkBBBBBCCCCCCCCCCCC"),

    LT_Lithuania("LTkkBBBBBCCCCCCCCCCC"),

    LU_Luxembourg("LUkkBBBCCCCCCCCCCCCC"),

    MK_Macedonia("MKkkBBBCCCCCCCCCCRR"),

    MT_Malta("MTkkBBBBSSSSSCCCCCCCCCCCCCCCCCC"),

    MR_Mauritania("MRkkBBBBBSSSSSCCCCCCCCCCCRR"),

    MU_Mauritius("MUkkBBBBBBSSCCCCCCCCCCCCCCCCCC"),

    MD_Moldova("MDkkBBCCCCCCCCCCCCCCCCCC"),

    MC_Monaco("MCkkBBBBBSSSSSCCCCCCCCCCCRR"),

    ME_Montenegro("MEkkBBBCCCCCCCCCCCCCRR"),

    NL_Netherlands("NLkkBBBBCCCCCCCCCC"),

    NO_Norway("NOkkBBBBCCCCCCR"),

    PK_Pakistan("PKkkBBBBCCCCCCCCCCCCCCCC"),

    PS_Palestinian_Territory("PSkkBBBBKKKKKKKKKCCCCCCCCCCCC"),

    PL_Poland("PLkkBBBSSSSRCCCCCCCCCCCCCCCC"),

    PT_Portugal("PTkkBBBBSSSSCCCCCCCCCCCRR"),

    QA_Qatar("QAkkBBBBCCCCCCCCCCCCCCCCCCCCC"),

    RO_Romania("ROkkBBBBCCCCCCCCCCCCCCCC"),

    SM_San_Marino("SMkkRBBBBBSSSSSCCCCCCCCCCCC"),

    SA_Saoudi_Arabia("SAkkBBCCCCCCCCCCCCCCCCCC"),

    RS_Serbia("RSkkBBBCCCCCCCCCCCCCRR"),

    SK_Slovakia("SKkkBBBBCCCCCCCCCCCCCCCC"),

    SI_Slovenia("SIkkBBSSSCCCCCCCCRR"),

    ES_Spain("ESkkBBBBSSSSRRCCCCCCCCCC"),

    SE_Sweden("SEkkBBBCCCCCCCCCCCCCCCCR"),

    CH_Switzerland("CHkkBBBBBCCCCCCCCCCCC"),

    TL_East_Timor("TLkkBBBCCCCCCCCCCCCCCRR"),

    TR_Turkey("TRkkBBBBBRCCCCCCCCCCCCCCCC"),

    TN_Tunisia("TNkkBBSSSCCCCCCCCCCCCCCC"),

    AE_United_arab_emirates("AEkkBBBCCCCCCCCCCCCCCCC"),

    GB_United_Kingdom("GBkkBBBBSSSSSSCCCCCCCC", "JE", "GG", "IM"),

    VG_Virgin_Islands_British("VGkkBBBBCCCCCCCCCCCCCCCC");

    private String[] countryCodes;
    private String format;

    Iban(String format, String... extraCountryCodes) {
        this.countryCodes = Arrays.copyOf(extraCountryCodes, extraCountryCodes.length + 1);
        this.countryCodes[extraCountryCodes.length] = format.substring(0, 2);
        this.format = format;
    }

    private static int getIbanLengthForCountry(String possibleIban) {
        Iban i = getIbanForCountry(possibleIban);
        if (i != null) {
            return i.format.length();
        }
        return -1;
    }

    private static Iban getIbanForCountry(String possibleIban) {
        if (StringUtils.isEmptyOrNull(possibleIban) || possibleIban.length() < 2) {
            return null;
        }
        String countryCode = possibleIban.substring(0, 2);
        for (Iban i : values()) {
            for (String cc : i.countryCodes) {
                if (cc.equals(countryCode)) {
                    return i;
                }
            }
        }
        return null;
    }

    public static boolean validate(String iban) {
        iban = iban.replaceAll(" ", "");
        if (iban.length() < 2) {
            return false;
        }
        if (getIbanLengthForCountry(iban) != iban.length()) {
            return false;
        }
        return true;
    }

}

