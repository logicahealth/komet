/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.lang;


/**
 * The Enum LanguageCode represents a two or six character representation of the
 * language or language and dialect code respectively. If the language has a
 * dialect, the dialect is represented by a dash and two characters following
 * the language.
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes</a>
 */
public enum LanguageCode {

    /**
     * English.
     */
    EN,
    /**
     * English (Australia).
     */
    EN_AU,
    /**
     * English (Belize).
     *
     */
    EN_BZ,
    /**
     * English (Canadian).
     */
    EN_CA,
    /**
     * English (United Kingdom).
     */
    EN_GB,
    /**
     * English (Ireland).
     */
    EN_IE,
    /**
     * English (Jamaica).
     */
    EN_JM,
    /**
     * English (New Zealand).
     */
    EN_NZ,
    /**
     * English (Trinidad).
     */
    EN_TT,
    /**
     * English (United States).
     *
     */
    EN_US,
    /**
     * English (South African).
     */
    EN_ZA,
    /**
     * Spanish.
     */
    ES,
    /**
     * Spanish (Argentina).
     */
    ES_AR,
    /**
     * Spanish (Bolivia).
     */
    ES_BO,
    /**
     * Spanish (Chile).
     */
    ES_CL,
    /**
     * Spanish (Colombia).
     */
    ES_CO,
    /**
     * Spanish (Costa Rica).
     */
    ES_CR,
    /**
     * Spanish (Dominican Republic).
     */
    ES_DO,
    /**
     * Spanish (Ecuador).
     */
    ES_EC,
    /**
     * Spanish (Spain).
     */
    ES_ES,
    /**
     * Spanish (Guatemala).
     */
    ES_GT,
    /**
     * Spanish (Honduras).
     */
    ES_HN,
    /**
     * Spanish (Mexico).
     */
    EX_MX,
    /**
     * Spanish (Nicaragua).
     */
    ES_NI,
    /**
     * Spanish (Panama).
     */
    ES_PA,
    /**
     * Spanish (Peru).
     */
    ES_PE,
    /**
     * Spanish (Paraguay).
     */
    ES_PY,
    /**
     * Spanish (El Salvador).
     */
    ES_SV,
    /**
     * Spanish (Uruguay).
     */
    ES_UY,
    /**
     * Spanish (Venezuela).
     */
    ES_VE,
    /**
     * French.
     */
    FR,
    /**
     * French (Belgium).
     */
    FR_BE,
    /**
     * French (Canada).
     */
    FR_CA,
    /**
     * French (France).
     */
    FR_FR,
    /**
     * French (Switzerland).
     */
    FR_CH,
    /**
     * French (Luxembourg).
     */
    FR_LU,
    /**
     * French (Monaco).
     */
    FR_MC,
    /**
     * Danish.
     */
    DA,
    /**
     * Danish (Denmark).
     */
    DA_DK,
    /**
     * Polish.
     */
    PL,
    /**
     * Dutch.
     */
    NL,
    /**
     * Swedish.
     */
    SV,
    /**
     * Swedish (Finland).
     */
    SV_FI,
    /**
     * Swedish (Sweden).
     */
    SV_SE,
    /**
     * Lithuanian.
     */
    LT,
    /**
     * Lithuanian (Lithuania).
     */
    LT_LT,
    /**
     * Chinese.
     */
    ZH,
    /**
     * Chinese (S).
     */
    ZH_CN,
    /**
     * Chinese (Hong Kong).
     */
    ZH_HK,
    /**
     * Chinese (Simplified).
     */
    ZH_CHS,
    /**
     * Chinese (Traditional).
     */
    ZH_CHT,
    /**
     * Chinese (Macau).
     */
    ZH_MO,
    /**
     * Chinese (Singapore).
     */
    ZH_SG,
    /**
     * Chinese (Taiwan).
     */
    ZH_TW,
    /**
     * Unspecified.
     */
    ZZ;

    /**
     * Gets a formated language code.
     *
     * @return the string representing the language code and dialect code
     */
    public String getFormatedLanguageCode() {
        String result = "";
        String name = this.name();
        String[] nameArray = name.split("_");
        if (nameArray.length == 1) {
            result = nameArray[0].toLowerCase();
        } else if (nameArray.length == 2) {
            result = nameArray[0].toLowerCase() + "-" + nameArray[1].toUpperCase();
        }
        return result;
    }

    /**
     * Gets a formated language without a dialect code.
     *
     * @return a two character string representing the language code with no dialect code
     */
    public String getFormatedLanguageNoDialectCode() {
        String name = this.name();
        String[] nameArray = name.split("_");
        return nameArray[0].toLowerCase();
    }

    /**
     * Gets a <code>LanguageCode</code> representing the given <code>name</code>.
     *
     * @param name the two or six character string representing the language code
     * @return the specified LanguageCode
     */
    public static LanguageCode getLangCode(String name) {
        String result = null;
        String[] nameArray = name.split("-");
        if (nameArray.length == 1) {
            result = nameArray[0].toUpperCase();
        } else if (nameArray.length == 2) {
            result = nameArray[0].toUpperCase() + "_" + nameArray[1].toUpperCase();
        }
        return LanguageCode.valueOf(result);
    }

    /**
     * Returns a string representation of this LanguageCode.
     * 
     * @return a string representation of this LanguageCode
     * @see LanguageCode#name() 
     */
    @Override
    public String toString() {
        return this.name();
    }
}