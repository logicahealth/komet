package sh.komet.converter.turtle;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCode;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class TempLanguageMap {
    /**
     * Gets the concept for language code.
     *
     * @param lc the lc
     * @return the concept for language code
     */
    public static ConceptSpecification getConceptForLanguageCode(LanguageCode lc) {
        switch (lc) {
            case CS:
                return MetaData.CZECH_LANGUAGE____SOLOR;
            case EN:
            case EN_AU:
            case EN_BZ:
            case EN_CA:
            case EN_GB:
            case EN_IE:
            case EN_JM:
            case EN_NZ:
            case EN_TT:
            case EN_US:
            case EN_ZA:
                return MetaData.ENGLISH_LANGUAGE____SOLOR;

            case ES:
            case ES_AR:
            case ES_BO:
            case ES_CL:
            case ES_CO:
            case ES_CR:
            case ES_DO:
            case ES_EC:
            case ES_ES:
            case ES_GT:
            case ES_HN:
            case ES_NI:
            case ES_MX:
            case ES_PA:
            case ES_PE:
            case ES_PY:
            case ES_SV:
            case ES_UY:
            case ES_VE:
                return MetaData.SPANISH_LANGUAGE____SOLOR;

            case DA:
            case DA_DK:
                return MetaData.DANISH_LANGUAGE____SOLOR;

            case DE:
                return MetaData.GERMAN_LANGUAGE____SOLOR;
            case FR:
            case FR_BE:
            case FR_CA:
            case FR_CH:
            case FR_FR:
            case FR_LU:
            case FR_MC:
                return MetaData.FRENCH_LANGUAGE____SOLOR;
            case GA:
                return MetaData.IRISH_LANGUAGE____SOLOR;
            case IT:
                return MetaData.ITALIAN_LANGUAGE____SOLOR;
            case KO:
                return MetaData.KOREAN_LANGUAGE____SOLOR;
            case LT:
            case LT_LT:
                return MetaData.LITHUANIAN_LANGUAGE____SOLOR;

            case NL:
                return MetaData.DUTCH_LANGUAGE____SOLOR;

            case PL:
                return MetaData.POLISH_LANGUAGE____SOLOR;

            case RU:
                return MetaData.RUSSIAN_LANGUAGE____SOLOR;

            case SV:
            case SV_FI:
            case SV_SE:
                return MetaData.SWEDISH_LANGUAGE____SOLOR;

            case ZH:
            case ZH_CHS:
            case ZH_CHT:
            case ZH_CN:
            case ZH_HK:
            case ZH_MO:
            case ZH_SG:
            case ZH_TW:
                return MetaData.CHINESE_LANGUAGE____SOLOR;

            case ZZ:
            default:
                throw new RuntimeException("Unmapped Language Code " + lc);
        }
    }

    /**
     * Gets the concept dialect for language code.
     * Note, this method is really incomplete.
     * TODO get rid of all of this mapping, and just properly load an iso table as part of the metadata...
     *
     * @param lc the lc
     * @return the concept for dialect language code
     */
    public static ConceptSpecification getConceptDialectForLanguageCode(LanguageCode lc) {
        switch (lc) {
            case CS:
                return MetaData.CZECH_DIALECT____SOLOR;
            case EN_GB:
                return MetaData.GB_ENGLISH_DIALECT____SOLOR;
            case EN_US:
                return MetaData.US_ENGLISH_DIALECT____SOLOR;
            case EN:
            case EN_AU:
            case EN_BZ:
            case EN_CA:
            case EN_IE:
            case EN_JM:
            case EN_NZ:
            case EN_TT:
            case EN_ZA:
                return MetaData.ENGLISH_DIALECT_ASSEMBLAGE____SOLOR;
            case ES:
            case ES_AR:
            case ES_BO:
            case ES_CL:
            case ES_CO:
            case ES_CR:
            case ES_DO:
            case ES_EC:
            case ES_ES:
            case ES_GT:
            case ES_HN:
            case ES_NI:
            case ES_MX:
            case ES_PA:
            case ES_PE:
            case ES_PY:
            case ES_SV:
            case ES_UY:
            case ES_VE:
                return MetaData.SPANISH_DIALECT_ASSEMBLAGE____SOLOR;
            case GA:
                return MetaData.IRISH_DIALECT____SOLOR;
            case KO:
                return MetaData.KOREAN_DIALECT____SOLOR;
            case FR:
            case FR_BE:
            case FR_CA:
            case FR_CH:
            case FR_FR:
            case FR_LU:
            case FR_MC:
                return MetaData.FRENCH_DIALECT____SOLOR;
            case RU:
                return MetaData.RUSSIAN_DIALECT____SOLOR;
            case PL:
                return MetaData.POLISH_DIALECT____SOLOR;
            case DA:
            case DA_DK:
            case LT:
            case LT_LT:
            case NL:
            case SV:
            case SV_FI:
            case SV_SE:
            case ZH:
            case ZH_CHS:
            case ZH_CHT:
            case ZH_CN:
            case ZH_HK:
            case ZH_MO:
            case ZH_SG:
            case ZH_TW:
            case ZZ:
            default:
                throw new RuntimeException("Unmapped Language Dialect " + lc);
        }
    }

    /**
     * Concept nid to iso 639.
     *
     * @param nid the nid
     * @return the string
     */
    public static String conceptNidToIso639(int nid) {
        if (nid >= 0) {
            throw new IllegalStateException("Nids must be negative: " + nid);
        }

        if (TermAux.ENGLISH_LANGUAGE.getNid() == nid) {
            return "en";
        }

        if (TermAux.SPANISH_LANGUAGE.getNid() == nid) {
            return "es";
        }

        if (TermAux.FRENCH_LANGUAGE.getNid() == nid) {
            return "fr";
        }

        if (TermAux.DANISH_LANGUAGE.getNid() == nid) {
            return "da";
        }

        if (TermAux.POLISH_LANGUAGE.getNid() == nid) {
            return "pl";
        }

        if (TermAux.DUTCH_LANGUAGE.getNid() == nid) {
            return "nl";
        }

        if (TermAux.LITHUANIAN_LANGUAGE.getNid() == nid) {
            return "lt";
        }

        if (TermAux.CHINESE_LANGUAGE.getNid() == nid) {
            return "zh";
        }

        if (TermAux.JAPANESE_LANGUAGE.getNid() == nid) {
            return "ja";
        }

        if (TermAux.SWEDISH_LANGUAGE.getNid() == nid) {
            return "sv";
        }

        if (MetaData.KOREAN_LANGUAGE____SOLOR.getNid() == nid) {
            return "ko";
        }

        if (MetaData.RUSSIAN_LANGUAGE____SOLOR.getNid() == nid) {
            return "ru";
        }

        if (MetaData.IRISH_LANGUAGE____SOLOR.getNid() == nid) {
            return "ga";
        }

        if (MetaData.CZECH_LANGUAGE____SOLOR.getNid() == nid) {
            return "cs";
        }

        if (MetaData.ITALIAN_LANGUAGE____SOLOR.getNid() == nid) {
            return "it";
        }

        if (MetaData.GERMAN_LANGUAGE____SOLOR.getNid() == nid) {
            return "de";
        }

        throw new UnsupportedOperationException("Can't convert " + nid + " to an iso639 code");
    }

    /**
     * Iso 639 to concept nid.
     *
     * @param iso639text the iso 639 text
     * @return the int
     */
    public static int iso639toConceptNid(String iso639text) {
        //TODO we should really get rid of all of this hard-coded stuff and replace it with putting proper language codes
        //directly into the metadata concept definitions, where they should be, so this can just be a query....
        switch (iso639text.toLowerCase(Locale.ENGLISH)) {
            case "en":
                return Get.identifierService()
                        .getNidForUuids(TermAux.ENGLISH_LANGUAGE.getUuids());

            case "es":
                return Get.identifierService()
                        .getNidForUuids(TermAux.SPANISH_LANGUAGE.getUuids());

            case "fr":
                return Get.identifierService()
                        .getNidForUuids(TermAux.FRENCH_LANGUAGE.getUuids());

            case "da":
                return Get.identifierService()
                        .getNidForUuids(TermAux.DANISH_LANGUAGE.getUuids());

            case "pl":
                return Get.identifierService()
                        .getNidForUuids(TermAux.POLISH_LANGUAGE.getUuids());

            case "nl":
                return Get.identifierService()
                        .getNidForUuids(TermAux.DUTCH_LANGUAGE.getUuids());

            case "lt":
                return Get.identifierService()
                        .getNidForUuids(TermAux.LITHUANIAN_LANGUAGE.getUuids());

            case "zh":
                return Get.identifierService()
                        .getNidForUuids(TermAux.CHINESE_LANGUAGE.getUuids());

            case "ja":
                return Get.identifierService()
                        .getNidForUuids(TermAux.JAPANESE_LANGUAGE.getUuids());

            case "sv":
                return Get.identifierService()
                        .getNidForUuids(TermAux.SWEDISH_LANGUAGE.getUuids());

            default:
                throw new UnsupportedOperationException("s Can't handle: " + iso639text);
        }
    }

    /**
     * Iso 639 to assemblage nid.
     *
     * @param iso639text the iso 639 text
     * @return the int
     */
    public static int iso639toDescriptionAssemblageNid(String iso639text) {
        switch (iso639text.toLowerCase(Locale.ENGLISH)) {
            case "en":
                return TermAux.ENGLISH_LANGUAGE.getNid();

            case "es":
                return TermAux.SPANISH_LANGUAGE.getNid();

            case "fr":
                return TermAux.FRENCH_LANGUAGE.getNid();

            case "da":
                return TermAux.DANISH_LANGUAGE.getNid();

            case "pl":
                return TermAux.POLISH_LANGUAGE.getNid();

            case "nl":
                return TermAux.DUTCH_LANGUAGE.getNid();

            case "lt":
                return TermAux.LITHUANIAN_LANGUAGE.getNid();

            case "zh":
                return TermAux.CHINESE_LANGUAGE.getNid();

            case "ja":
                return TermAux.JAPANESE_LANGUAGE.getNid();

            case "sv":
                return TermAux.SWEDISH_LANGUAGE.getNid();

            default:
                throw new UnsupportedOperationException("Can't handle: " + iso639text);
        }
    }


    /**
     * Gets the language code for UUID.
     *
     * @param uuid the uuid
     * @return the language code for UUID
     */
    public static Optional<LanguageCode> getLanguageCodeForUUID(UUID uuid) {
        for (final LanguageCode lc: LanguageCode.values()) {
            if (lc == LanguageCode.ZZ) {
                continue;
            }

            for (final UUID itemUuid: getConceptForLanguageCode(lc).getUuids()) {
                if (itemUuid.equals(uuid)) {
                    return Optional.of(lc);
                }
            }
        }

        return Optional.empty();
    }
}
