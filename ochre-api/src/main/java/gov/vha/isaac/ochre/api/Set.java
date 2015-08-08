/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api;

/**
 *
 * @author kec
 */
public class Set {

    /**
     * Sets the default user for editing and role-based access control. When
     * changed, other default objects that reference this object will be updated
     * accordingly. Default: The value to use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultUser(int conceptId) {
        Get.configurationService().setDefaultUser(conceptId);
    }

    /**
     * Sets the default module for editing operations. When changed, other
     * default objects that reference this object will be updated accordingly.
     * Default: The value to use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultModule(int conceptId){
        Get.configurationService().setDefaultModule(conceptId);
    }
    /**
     * Sets the default path for editing operations. When changed, other default
     * objects that reference this object will be updated accordingly. Default:
     * The value to use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultPath(int conceptId){
        Get.configurationService().setDefaultPath(conceptId);
    }

    /**
     * Sets the default language for description retrieval. When changed, other
     * default objects that reference this object will be updated accordingly.
     * Default: The value to use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultLanguage(int conceptId){
        Get.configurationService().setDefaultLanguage(conceptId);
    }

    /**
     * Sets the default dialect preference list for description retrieval. When
     * changed, other default objects that reference this object will be updated
     * accordingly. Default: The value to use if another value is not provided.
     *
     * @param dialectAssemblagePreferenceList prioritized preference list of
     * dialect assemblage sequences
     */
    public static void defaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList){
        Get.configurationService().setDefaultDialectAssemblagePreferenceList(dialectAssemblagePreferenceList);
    }

    /**
     * Sets the default description type preference list for description
     * retrieval. When changed, other default objects that reference this object
     * will be updated accordingly. Default: The value to use if another value
     * is not provided.
     *
     * @param descriptionTypePreferenceList prioritized preference list of
     * description type sequences
     */
    public static void defaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
        Get.configurationService().setDefaultDescriptionTypePreferenceList(descriptionTypePreferenceList);
    }

    /**
     * Sets the default stated definition assemblage. When changed, other
     * default objects that reference this object will be updated accordingly.
     * Default: The value to use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultStatedAssemblage(int conceptId){
        Get.configurationService().setDefaultStatedAssemblage(conceptId);
    }

    /**
     * Sets the default inferred definition assemblage. When changed, other
     * default objects that reference this object will be updated accordingly.
     * Default: The value to use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultInferredAssemblage(int conceptId){
        Get.configurationService().setDefaultInferredAssemblage(conceptId);
    }

    /**
     * Sets the default description-logic profile. When changed, other default
     * objects that reference this object will be updated accordingly. Default:
     * The value to use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultDescriptionLogicProfile(int conceptId){
        Get.configurationService().setDefaultDescriptionLogicProfile(conceptId);
    }

    /**
     * Sets the default classifier. When changed, other default objects that
     * reference this object will be updated accordingly. Default: The value to
     * use if another value is not provided.
     *
     * @param conceptId either a nid or conceptSequence
     */
    public static void defaultClassifier(int conceptId){
        Get.configurationService().setDefaultClassifier(conceptId);
    }

    /**
     * Sets the default time for viewing versions of components When changed,
     * other default objects that reference this object will be updated
     * accordingly. Default: The value to use if another value is not provided.
     *
     * @param timeInMs Time in milliseconds since unix epoch. Long.MAX_VALUE is
     * used to represent the latest versions.
     */
    public static void defaultTime(long timeInMs){
        Get.configurationService().setDefaultTime(timeInMs);
    }

}
