/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model.configuration;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.model.coordinate.LanguageCoordinateImpl;

/**
 *
 * @author kec
 */
public class LanguageCoordinates {
    public static LanguageCoordinate getUsEnglishLanguagePreferredTermCoordinate() {
        int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
        int[] dialectAssemblagePreferenceList = new int[] {
            TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence(),
            TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence()
        };
        int[] descriptionTypePreferenceList = new int[] {
            TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence(),
            TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence()
        };
        
        return new LanguageCoordinateImpl(languageSequence, 
                dialectAssemblagePreferenceList, descriptionTypePreferenceList);
    }

    public static LanguageCoordinate getUsEnglishLanguageFullySpecifiedNameCoordinate() {
        int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
        int[] dialectAssemblagePreferenceList = new int[] {
            TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence(),
            TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence()
        };
        int[] descriptionTypePreferenceList = new int[] {
            TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence(),
            TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence()
        };
        
        return new LanguageCoordinateImpl(languageSequence, 
                dialectAssemblagePreferenceList, descriptionTypePreferenceList);
    }
    public static LanguageCoordinate getGbEnglishLanguagePreferredTermCoordinate() {
        int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
        int[] dialectAssemblagePreferenceList = new int[] {
            TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence(),
            TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence()
        };
        int[] descriptionTypePreferenceList = new int[] {
            TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence(),
            TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence()
        };
        
        return new LanguageCoordinateImpl(languageSequence, 
                dialectAssemblagePreferenceList, descriptionTypePreferenceList);
    }

    public static LanguageCoordinate getGbEnglishLanguageFullySpecifiedNameCoordinate() {
        int languageSequence = TermAux.ENGLISH_LANGUAGE.getConceptSequence();
        int[] dialectAssemblagePreferenceList = new int[] {
            TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence(),
            TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence()
        };
        int[] descriptionTypePreferenceList = new int[] {
            TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence(),
            TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence()
        };
        
        return new LanguageCoordinateImpl(languageSequence, 
                dialectAssemblagePreferenceList, descriptionTypePreferenceList);
    }
    
    public static int iso639toConceptNid(String iso639text) {
        switch (iso639text.toLowerCase()) {
            case "en":
                return Get.identifierService().getNidForUuids(TermAux.ENGLISH_LANGUAGE.getUuids());
            case "es":
                return Get.identifierService().getNidForUuids(TermAux.SPANISH_LANGUAGE.getUuids());
            case "fr": 
                return Get.identifierService().getNidForUuids(TermAux.FRENCH_LANGUAGE.getUuids());
            case "da":
                return Get.identifierService().getNidForUuids(TermAux.DANISH_LANGUAGE.getUuids());
            case "pl":
                return Get.identifierService().getNidForUuids(TermAux.POLISH_LANGUAGE.getUuids());
            case "nl":
                return Get.identifierService().getNidForUuids(TermAux.DUTCH_LANGUAGE.getUuids());
            case "lt":
                return Get.identifierService().getNidForUuids(TermAux.LITHUANIAN_LANGUAGE.getUuids());
            case "zh":
                return Get.identifierService().getNidForUuids(TermAux.CHINESE_LANGUAGE.getUuids());
            case "ja":
                return Get.identifierService().getNidForUuids(TermAux.JAPANESE_LANGUAGE.getUuids());
            case "sv":
                return Get.identifierService().getNidForUuids(TermAux.SWEDISH_LANGUAGE.getUuids());
            default: 
                throw new UnsupportedOperationException("Can't handle: " + iso639text);
        }
    }
    public static int iso639toConceptSequence(String iso639text) {
        switch (iso639text.toLowerCase()) {
            case "en":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.ENGLISH_LANGUAGE.getUuids());
            case "es":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.SPANISH_LANGUAGE.getUuids());
            case "fr": 
                return Get.identifierService().getConceptSequenceForUuids(TermAux.FRENCH_LANGUAGE.getUuids());
            case "da":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.DANISH_LANGUAGE.getUuids());
            case "pl":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.POLISH_LANGUAGE.getUuids());
            case "nl":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.DUTCH_LANGUAGE.getUuids());
            case "lt":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.LITHUANIAN_LANGUAGE.getUuids());
            case "zh":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.CHINESE_LANGUAGE.getUuids());
            case "ja":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.JAPANESE_LANGUAGE.getUuids());
            case "sv":
                return Get.identifierService().getConceptSequenceForUuids(TermAux.SWEDISH_LANGUAGE.getUuids());
            default: 
                throw new UnsupportedOperationException("Can't handle: " + iso639text);
        }
    }
    
    public static String conceptNidToIso639(int nid) {
            if (nid >= 0) {
                nid = Get.identifierService().getConceptNid(nid);
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
            throw new UnsupportedOperationException("Can't handle: " + nid);
    }
    
    public static int caseSignificanceToConceptSequence(boolean initialCaseSignificant) {
        return TermAux.caseSignificanceToConceptSequence(initialCaseSignificant);
    }

    public static boolean conceptIdToCaseSignificance(int id) {
        return TermAux.conceptIdToCaseSignificance(id);
    }
}
