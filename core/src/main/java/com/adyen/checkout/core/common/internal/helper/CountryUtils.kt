/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/6/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CountryUtils {

    fun getLocalizedCountries(
        shopperLocale: Locale,
        allowedISOCodes: List<String>? = null,
        comparator: Comparator<CountryModel> = compareBy { it.countryName },
    ): List<CountryModel> {
        return getCountries(allowedISOCodes)
            .map {
                CountryModel(
                    isoCode = it.isoCode,
                    countryName = getCountryName(it.isoCode, shopperLocale),
                    callingCode = it.callingCode,
                )
            }
            .sortedWith(comparator)
    }

    /**
     * Returns the list of supported countries to display.
     *
     * @param allowedISOCodes the list of ISO codes to filter the countries with. No filtering is done if this param is
     * `null`.
     * @return the list of countries.
     */
    @JvmStatic
    fun getCountries(allowedISOCodes: List<String>? = null): List<CountryInfo> {
        if (allowedISOCodes == null) return countries
        return countries.filter { allowedISOCodes.contains(it.isoCode) }
    }

    /**
     * Returns the localized display name of a country
     *
     * @param isoCode the ISO code of the country.
     * @param locale the locale in which the country name should be localized.
     * @return the localized country name.
     */
    @JvmStatic
    fun getCountryName(isoCode: String, locale: Locale): String {
        val countryLocale = Locale("", isoCode)
        return countryLocale.getDisplayCountry(locale)
    }

    @VisibleForTesting
    internal val countries = listOf(
        CountryInfo(isoCode = "AD", callingCode = "+376"),
        CountryInfo(isoCode = "AE", callingCode = "+971"),
        CountryInfo(isoCode = "AF", callingCode = "+93"),
        CountryInfo(isoCode = "AG", callingCode = "+1268"),
        CountryInfo(isoCode = "AI", callingCode = "+1264"),
        CountryInfo(isoCode = "AL", callingCode = "+355"),
        CountryInfo(isoCode = "AM", callingCode = "+374"),
        CountryInfo(isoCode = "AN", callingCode = "+599"),
        CountryInfo(isoCode = "AO", callingCode = "+244"),
        CountryInfo(isoCode = "AQ", callingCode = "+672"),
        CountryInfo(isoCode = "AR", callingCode = "+54"),
        CountryInfo(isoCode = "AS", callingCode = "+1684"),
        CountryInfo(isoCode = "AT", callingCode = "+43"),
        CountryInfo(isoCode = "AU", callingCode = "+61"),
        CountryInfo(isoCode = "AW", callingCode = "+297"),
        CountryInfo(isoCode = "AX", callingCode = "+358"),
        CountryInfo(isoCode = "AZ", callingCode = "+994"),
        CountryInfo(isoCode = "BA", callingCode = "+387"),
        CountryInfo(isoCode = "BB", callingCode = "+1246"),
        CountryInfo(isoCode = "BD", callingCode = "+880"),
        CountryInfo(isoCode = "BE", callingCode = "+32"),
        CountryInfo(isoCode = "BF", callingCode = "+226"),
        CountryInfo(isoCode = "BG", callingCode = "+359"),
        CountryInfo(isoCode = "BH", callingCode = "+973"),
        CountryInfo(isoCode = "BI", callingCode = "+257"),
        CountryInfo(isoCode = "BJ", callingCode = "+229"),
        CountryInfo(isoCode = "BL", callingCode = "+590"),
        CountryInfo(isoCode = "BM", callingCode = "+1441"),
        CountryInfo(isoCode = "BN", callingCode = "+673"),
        CountryInfo(isoCode = "BO", callingCode = "+591"),
        CountryInfo(isoCode = "BR", callingCode = "+55"),
        CountryInfo(isoCode = "BS", callingCode = "+1242"),
        CountryInfo(isoCode = "BT", callingCode = "+975"),
        CountryInfo(isoCode = "BW", callingCode = "+267"),
        CountryInfo(isoCode = "BY", callingCode = "+375"),
        CountryInfo(isoCode = "BZ", callingCode = "+501"),
        CountryInfo(isoCode = "CA", callingCode = "+1"),
        CountryInfo(isoCode = "CC", callingCode = "+61"),
        CountryInfo(isoCode = "CD", callingCode = "+243"),
        CountryInfo(isoCode = "CF", callingCode = "+236"),
        CountryInfo(isoCode = "CG", callingCode = "+242"),
        CountryInfo(isoCode = "CH", callingCode = "+41"),
        CountryInfo(isoCode = "CI", callingCode = "+225"),
        CountryInfo(isoCode = "CK", callingCode = "+682"),
        CountryInfo(isoCode = "CL", callingCode = "+56"),
        CountryInfo(isoCode = "CM", callingCode = "+237"),
        CountryInfo(isoCode = "CN", callingCode = "+86"),
        CountryInfo(isoCode = "CO", callingCode = "+57"),
        CountryInfo(isoCode = "CR", callingCode = "+506"),
        CountryInfo(isoCode = "CU", callingCode = "+53"),
        CountryInfo(isoCode = "CV", callingCode = "+238"),
        CountryInfo(isoCode = "CX", callingCode = "+61"),
        CountryInfo(isoCode = "CY", callingCode = "+537"),
        CountryInfo(isoCode = "CZ", callingCode = "+420"),
        CountryInfo(isoCode = "DE", callingCode = "+49"),
        CountryInfo(isoCode = "DJ", callingCode = "+253"),
        CountryInfo(isoCode = "DK", callingCode = "+45"),
        CountryInfo(isoCode = "DM", callingCode = "+1767"),
        CountryInfo(isoCode = "DO", callingCode = "+1849"),
        CountryInfo(isoCode = "DZ", callingCode = "+213"),
        CountryInfo(isoCode = "EC", callingCode = "+593"),
        CountryInfo(isoCode = "EE", callingCode = "+372"),
        CountryInfo(isoCode = "EG", callingCode = "+20"),
        CountryInfo(isoCode = "ER", callingCode = "+291"),
        CountryInfo(isoCode = "ES", callingCode = "+34"),
        CountryInfo(isoCode = "ET", callingCode = "+251"),
        CountryInfo(isoCode = "FI", callingCode = "+358"),
        CountryInfo(isoCode = "FJ", callingCode = "+679"),
        CountryInfo(isoCode = "FK", callingCode = "+500"),
        CountryInfo(isoCode = "FM", callingCode = "+691"),
        CountryInfo(isoCode = "FO", callingCode = "+298"),
        CountryInfo(isoCode = "FR", callingCode = "+33"),
        CountryInfo(isoCode = "GA", callingCode = "+241"),
        CountryInfo(isoCode = "GB", callingCode = "+44"),
        CountryInfo(isoCode = "GD", callingCode = "+1473"),
        CountryInfo(isoCode = "GE", callingCode = "+995"),
        CountryInfo(isoCode = "GF", callingCode = "+594"),
        CountryInfo(isoCode = "GG", callingCode = "+44"),
        CountryInfo(isoCode = "GH", callingCode = "+233"),
        CountryInfo(isoCode = "GI", callingCode = "+350"),
        CountryInfo(isoCode = "GL", callingCode = "+299"),
        CountryInfo(isoCode = "GM", callingCode = "+220"),
        CountryInfo(isoCode = "GN", callingCode = "+224"),
        CountryInfo(isoCode = "GP", callingCode = "+590"),
        CountryInfo(isoCode = "GQ", callingCode = "+240"),
        CountryInfo(isoCode = "GR", callingCode = "+30"),
        CountryInfo(isoCode = "GS", callingCode = "+500"),
        CountryInfo(isoCode = "GT", callingCode = "+502"),
        CountryInfo(isoCode = "GU", callingCode = "+1671"),
        CountryInfo(isoCode = "GW", callingCode = "+245"),
        CountryInfo(isoCode = "GY", callingCode = "+595"),
        CountryInfo(isoCode = "HK", callingCode = "+852"),
        CountryInfo(isoCode = "HN", callingCode = "+504"),
        CountryInfo(isoCode = "HR", callingCode = "+385"),
        CountryInfo(isoCode = "HT", callingCode = "+509"),
        CountryInfo(isoCode = "HU", callingCode = "+36"),
        CountryInfo(isoCode = "ID", callingCode = "+62"),
        CountryInfo(isoCode = "IE", callingCode = "+353"),
        CountryInfo(isoCode = "IL", callingCode = "+972"),
        CountryInfo(isoCode = "IM", callingCode = "+44"),
        CountryInfo(isoCode = "IN", callingCode = "+91"),
        CountryInfo(isoCode = "IO", callingCode = "+246"),
        CountryInfo(isoCode = "IQ", callingCode = "+964"),
        CountryInfo(isoCode = "IR", callingCode = "+98"),
        CountryInfo(isoCode = "IS", callingCode = "+354"),
        CountryInfo(isoCode = "IT", callingCode = "+39"),
        CountryInfo(isoCode = "JE", callingCode = "+44"),
        CountryInfo(isoCode = "JM", callingCode = "+1876"),
        CountryInfo(isoCode = "JO", callingCode = "+962"),
        CountryInfo(isoCode = "JP", callingCode = "+81"),
        CountryInfo(isoCode = "KE", callingCode = "+254"),
        CountryInfo(isoCode = "KG", callingCode = "+996"),
        CountryInfo(isoCode = "KH", callingCode = "+855"),
        CountryInfo(isoCode = "KI", callingCode = "+686"),
        CountryInfo(isoCode = "KM", callingCode = "+269"),
        CountryInfo(isoCode = "KN", callingCode = "+1869"),
        CountryInfo(isoCode = "KP", callingCode = "+850"),
        CountryInfo(isoCode = "KR", callingCode = "+82"),
        CountryInfo(isoCode = "KW", callingCode = "+965"),
        CountryInfo(isoCode = "KY", callingCode = "+345"),
        CountryInfo(isoCode = "KZ", callingCode = "+77"),
        CountryInfo(isoCode = "LA", callingCode = "+856"),
        CountryInfo(isoCode = "LB", callingCode = "+961"),
        CountryInfo(isoCode = "LC", callingCode = "+1758"),
        CountryInfo(isoCode = "LI", callingCode = "+423"),
        CountryInfo(isoCode = "LK", callingCode = "+94"),
        CountryInfo(isoCode = "LR", callingCode = "+231"),
        CountryInfo(isoCode = "LS", callingCode = "+266"),
        CountryInfo(isoCode = "LT", callingCode = "+370"),
        CountryInfo(isoCode = "LU", callingCode = "+352"),
        CountryInfo(isoCode = "LV", callingCode = "+371"),
        CountryInfo(isoCode = "LY", callingCode = "+218"),
        CountryInfo(isoCode = "MA", callingCode = "+212"),
        CountryInfo(isoCode = "MC", callingCode = "+377"),
        CountryInfo(isoCode = "MD", callingCode = "+373"),
        CountryInfo(isoCode = "ME", callingCode = "+382"),
        CountryInfo(isoCode = "MF", callingCode = "+590"),
        CountryInfo(isoCode = "MG", callingCode = "+261"),
        CountryInfo(isoCode = "MH", callingCode = "+692"),
        CountryInfo(isoCode = "MK", callingCode = "+389"),
        CountryInfo(isoCode = "ML", callingCode = "+223"),
        CountryInfo(isoCode = "MM", callingCode = "+95"),
        CountryInfo(isoCode = "MN", callingCode = "+976"),
        CountryInfo(isoCode = "MO", callingCode = "+853"),
        CountryInfo(isoCode = "MP", callingCode = "+1670"),
        CountryInfo(isoCode = "MQ", callingCode = "+596"),
        CountryInfo(isoCode = "MR", callingCode = "+222"),
        CountryInfo(isoCode = "MS", callingCode = "+1664"),
        CountryInfo(isoCode = "MT", callingCode = "+356"),
        CountryInfo(isoCode = "MU", callingCode = "+230"),
        CountryInfo(isoCode = "MV", callingCode = "+960"),
        CountryInfo(isoCode = "MW", callingCode = "+265"),
        CountryInfo(isoCode = "MX", callingCode = "+52"),
        CountryInfo(isoCode = "MY", callingCode = "+60"),
        CountryInfo(isoCode = "MZ", callingCode = "+258"),
        CountryInfo(isoCode = "NA", callingCode = "+264"),
        CountryInfo(isoCode = "NC", callingCode = "+687"),
        CountryInfo(isoCode = "NE", callingCode = "+227"),
        CountryInfo(isoCode = "NF", callingCode = "+672"),
        CountryInfo(isoCode = "NG", callingCode = "+234"),
        CountryInfo(isoCode = "NI", callingCode = "+505"),
        CountryInfo(isoCode = "NL", callingCode = "+31"),
        CountryInfo(isoCode = "NO", callingCode = "+47"),
        CountryInfo(isoCode = "NP", callingCode = "+977"),
        CountryInfo(isoCode = "NR", callingCode = "+674"),
        CountryInfo(isoCode = "NU", callingCode = "+683"),
        CountryInfo(isoCode = "NZ", callingCode = "+64"),
        CountryInfo(isoCode = "OM", callingCode = "+968"),
        CountryInfo(isoCode = "PA", callingCode = "+507"),
        CountryInfo(isoCode = "PE", callingCode = "+51"),
        CountryInfo(isoCode = "PF", callingCode = "+689"),
        CountryInfo(isoCode = "PG", callingCode = "+675"),
        CountryInfo(isoCode = "PH", callingCode = "+63"),
        CountryInfo(isoCode = "PK", callingCode = "+92"),
        CountryInfo(isoCode = "PL", callingCode = "+48"),
        CountryInfo(isoCode = "PM", callingCode = "+508"),
        CountryInfo(isoCode = "PN", callingCode = "+872"),
        CountryInfo(isoCode = "PR", callingCode = "+1939"),
        CountryInfo(isoCode = "PS", callingCode = "+970"),
        CountryInfo(isoCode = "PT", callingCode = "+351"),
        CountryInfo(isoCode = "PW", callingCode = "+680"),
        CountryInfo(isoCode = "PY", callingCode = "+595"),
        CountryInfo(isoCode = "QA", callingCode = "+974"),
        CountryInfo(isoCode = "RE", callingCode = "+262"),
        CountryInfo(isoCode = "RO", callingCode = "+40"),
        CountryInfo(isoCode = "RS", callingCode = "+381"),
        CountryInfo(isoCode = "RU", callingCode = "+7"),
        CountryInfo(isoCode = "RW", callingCode = "+250"),
        CountryInfo(isoCode = "SA", callingCode = "+966"),
        CountryInfo(isoCode = "SB", callingCode = "+677"),
        CountryInfo(isoCode = "SC", callingCode = "+248"),
        CountryInfo(isoCode = "SD", callingCode = "+249"),
        CountryInfo(isoCode = "SE", callingCode = "+46"),
        CountryInfo(isoCode = "SG", callingCode = "+65"),
        CountryInfo(isoCode = "SH", callingCode = "+290"),
        CountryInfo(isoCode = "SI", callingCode = "+386"),
        CountryInfo(isoCode = "SJ", callingCode = "+47"),
        CountryInfo(isoCode = "SK", callingCode = "+421"),
        CountryInfo(isoCode = "SL", callingCode = "+232"),
        CountryInfo(isoCode = "SM", callingCode = "+378"),
        CountryInfo(isoCode = "SN", callingCode = "+221"),
        CountryInfo(isoCode = "SO", callingCode = "+252"),
        CountryInfo(isoCode = "SR", callingCode = "+597"),
        CountryInfo(isoCode = "ST", callingCode = "+239"),
        CountryInfo(isoCode = "SV", callingCode = "+503"),
        CountryInfo(isoCode = "SY", callingCode = "+963"),
        CountryInfo(isoCode = "SZ", callingCode = "+268"),
        CountryInfo(isoCode = "TC", callingCode = "+1649"),
        CountryInfo(isoCode = "TD", callingCode = "+235"),
        CountryInfo(isoCode = "TG", callingCode = "+228"),
        CountryInfo(isoCode = "TH", callingCode = "+66"),
        CountryInfo(isoCode = "TJ", callingCode = "+992"),
        CountryInfo(isoCode = "TK", callingCode = "+690"),
        CountryInfo(isoCode = "TL", callingCode = "+670"),
        CountryInfo(isoCode = "TM", callingCode = "+993"),
        CountryInfo(isoCode = "TN", callingCode = "+216"),
        CountryInfo(isoCode = "TO", callingCode = "+676"),
        CountryInfo(isoCode = "TR", callingCode = "+90"),
        CountryInfo(isoCode = "TT", callingCode = "+1868"),
        CountryInfo(isoCode = "TV", callingCode = "+688"),
        CountryInfo(isoCode = "TW", callingCode = "+886"),
        CountryInfo(isoCode = "TZ", callingCode = "+255"),
        CountryInfo(isoCode = "UA", callingCode = "+380"),
        CountryInfo(isoCode = "UG", callingCode = "+256"),
        CountryInfo(isoCode = "US", callingCode = "+1"),
        CountryInfo(isoCode = "UY", callingCode = "+598"),
        CountryInfo(isoCode = "UZ", callingCode = "+998"),
        CountryInfo(isoCode = "VA", callingCode = "+379"),
        CountryInfo(isoCode = "VC", callingCode = "+1784"),
        CountryInfo(isoCode = "VE", callingCode = "+58"),
        CountryInfo(isoCode = "VG", callingCode = "+1284"),
        CountryInfo(isoCode = "VI", callingCode = "+1340"),
        CountryInfo(isoCode = "VN", callingCode = "+84"),
        CountryInfo(isoCode = "VU", callingCode = "+678"),
        CountryInfo(isoCode = "WF", callingCode = "+681"),
        CountryInfo(isoCode = "WS", callingCode = "+685"),
        CountryInfo(isoCode = "YE", callingCode = "+967"),
        CountryInfo(isoCode = "YT", callingCode = "+262"),
        CountryInfo(isoCode = "ZA", callingCode = "+27"),
        CountryInfo(isoCode = "ZM", callingCode = "+260"),
        CountryInfo(isoCode = "ZW", callingCode = "+263"),
    )
}

/**
 * Class that holds data about a given country
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CountryInfo(
    val isoCode: String,
    val callingCode: String,
)
