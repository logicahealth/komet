/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.model.coordinate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCoordinateService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.util.ArrayUtil;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;

/**
 * The Class LanguageCoordinateImpl.
 *
 * @author kec
 */
@XmlRootElement(name = "LanguageCoordinate")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"languageCoordinateUuid", "languageConcept","dialectAssemblageSpecPreferenceList", 
    "descriptionTypeSpecPreferenceList", "moduleSpecPreferenceListForLanguage"})
public class LanguageCoordinateImpl
        implements LanguageCoordinate {

    /**
     * The language concept nid.
     */
    ConceptSpecification languageConcept;

    /**
     * The dialect assemblage preference list.
     */
    ConceptSpecification[] dialectAssemblageSpecPreferenceList;

    /**
     * The description type preference list.
     */
    ConceptSpecification[] descriptionTypeSpecPreferenceList;

    ConceptSpecification[] moduleSpecPreferenceList;

    private final HashMap<Integer, LanguageCoordinate> altDescriptionTypeListCache = new HashMap<>();

    LanguageCoordinateImpl nextProrityLanguageCoordinate;

    /**
     * noarg for jaxb
     */
    public LanguageCoordinateImpl() {
    }

    
    public LanguageCoordinateImpl(ConceptSpecification languageConcept,
            ConceptSpecification[] dialectAssemblagePreferenceList,
            ConceptSpecification[] descriptionTypePreferenceList,
            ConceptSpecification[] modulePreferenceList) {
        this.languageConcept = languageConcept;
        this.dialectAssemblageSpecPreferenceList = dialectAssemblagePreferenceList;
        this.descriptionTypeSpecPreferenceList = descriptionTypePreferenceList;
        this.moduleSpecPreferenceList = modulePreferenceList;
    }

    /**
     * Instantiates a new language coordinate impl.
     *
     * @param languageConcept the language concept id
     * @param dialectAssemblagePreferenceList the dialect assemblage preference
     * list
     * @param descriptionTypePreferenceList the description type preference list
     * @param modulePreferenceList the module preference list. See
     * {@link LanguageCoordinate#getModulePreferenceListForLanguage()}
     */
    public LanguageCoordinateImpl(ConceptSpecification languageConcept,
            int[] dialectAssemblagePreferenceList,
            ConceptSpecification[] descriptionTypePreferenceList,
            int[] modulePreferenceList) {
        this(languageConcept,
                ArrayUtil.toSpecificationArray(dialectAssemblagePreferenceList),
                descriptionTypePreferenceList,
                ArrayUtil.toSpecificationArray(modulePreferenceList));
    }

    /**
     * Instantiates a new language coordinate impl, with an unspecified set of
     * modulePreferences.
     *
     * @param languageConcept the language concept
     * @param dialectAssemblagePreferenceList the dialect assemblage preference
     * list
     * @param descriptionTypePreferenceList the description type preference list
     */
    public LanguageCoordinateImpl(int languageConcept,
            int[] dialectAssemblagePreferenceList,
            ConceptSpecification[] descriptionTypePreferenceList) {
        this(new ConceptProxy(languageConcept), dialectAssemblagePreferenceList, descriptionTypePreferenceList, new int[]{});
    }

    @Override
    @XmlElement
    public UUID getLanguageCoordinateUuid() {
        return LanguageCoordinate.super.getLanguageCoordinateUuid(); //To change body of generated methods, choose Tools | Templates.
    }
  
    private void setLanguageCoordinateUuid(UUID uuid) {
        // noop for jaxb
    }

    @Override
    @XmlElement(name = "language", type=ConceptProxy.class)
    public ConceptSpecification getLanguageConcept() {
        return languageConcept;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final LanguageCoordinateImpl other = (LanguageCoordinateImpl) obj;

        if (this.languageConcept.getNid() != other.languageConcept.getNid()) {
            return false;
        }

        if (!Arrays.equals(this.dialectAssemblageSpecPreferenceList, other.dialectAssemblageSpecPreferenceList)) {
            return false;
        }

        if ((moduleSpecPreferenceList == null && other.moduleSpecPreferenceList != null)
                || (moduleSpecPreferenceList != null && other.moduleSpecPreferenceList == null)
                || moduleSpecPreferenceList != null && !Arrays.equals(this.moduleSpecPreferenceList, other.moduleSpecPreferenceList)) {
            return false;
        }

        return Arrays.equals(this.descriptionTypeSpecPreferenceList, other.descriptionTypeSpecPreferenceList);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int hash = 3;

        hash = 79 * hash + this.languageConcept.getNid();
        hash = 79 * hash + Arrays.hashCode(this.dialectAssemblageSpecPreferenceList);
        hash = 79 * hash + Arrays.hashCode(this.descriptionTypeSpecPreferenceList);
        hash = 79 * hash + (this.moduleSpecPreferenceList == null ? 0 : Arrays.hashCode(this.moduleSpecPreferenceList));
        return hash;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Language Coordinate{" + Get.conceptDescriptionText(this.languageConcept)
                + ", dialect preference: " + Get.conceptDescriptionTextList(this.dialectAssemblageSpecPreferenceList)
                + ", type preference: " + Get.conceptDescriptionTextList(this.descriptionTypeSpecPreferenceList)
                + ", module preference: " + Get.conceptDescriptionTextList(this.moduleSpecPreferenceList) + '}';
    }

    /**
     * @see
     * sh.isaac.api.coordinate.LanguageCoordinate#getDescription(java.util.List,
     * sh.isaac.api.coordinate.StampCoordinate) Implemented via
     * {@link LanguageCoordinateService#getSpecifiedDescription(StampCoordinate, List, LanguageCoordinate)}
     */
    @Override
    public LatestVersion<DescriptionVersion> getDescription(
            List<SemanticChronology> descriptionList,
            StampCoordinate stampCoordinate) {
        return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate, descriptionList, this);
    }

    /**
     *
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getDescription(int,
     * int[], sh.isaac.api.coordinate.StampCoordinate)
     */
    @Override
    public LatestVersion<DescriptionVersion> getDescription(int conceptNid, int[] descriptionTypePreference, StampCoordinate stampCoordinate) {
        Integer key = Arrays.hashCode(descriptionTypePreference);
        LanguageCoordinate lc = altDescriptionTypeListCache.get(key);
        if (lc == null) {
            lc = this.cloneAndChangeDescriptionType(descriptionTypePreference);
            altDescriptionTypeListCache.put(key, lc);
        }
        return lc.getDescription(conceptNid, stampCoordinate);
    }

    /**
     * @see
     * sh.isaac.api.coordinate.LanguageCoordinate#getDescription(java.util.List,
     * int[], sh.isaac.api.coordinate.StampCoordinate)
     */
    @Override
    public LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList, int[] descriptionTypePreference,
            StampCoordinate stampCoordinate) {
        Integer key = Arrays.hashCode(descriptionTypePreference);
        LanguageCoordinate lc = altDescriptionTypeListCache.get(key);
        if (lc == null) {
            lc = this.cloneAndChangeDescriptionType(descriptionTypePreference);
            altDescriptionTypeListCache.put(key, lc);
        }
        return lc.getDescription(descriptionList, stampCoordinate);
    }

    /**
     * Gets the description type preference list.
     *
     * @return the description type preference list
     */
    @Override
    public int[] getDescriptionTypePreferenceList() {
        return ArrayUtil.toNidArray(this.descriptionTypeSpecPreferenceList);
    }

    public void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
        this.descriptionTypeSpecPreferenceList = ArrayUtil.toSpecificationArray(descriptionTypePreferenceList);
        //Don't need to clear altDescriptionTypeListCache here, because its ignored anyway
    }

    /**
     * Same as {@link #setDescriptionTypePreferenceList(int[])}, except it also
     * makes the same call recursively on the preference list in
     * {@link #getNextProrityLanguageCoordinate()}, if any.
     *
     * @param descriptionTypeNidPreferenceList
     */
    public void setDescriptionTypePreferenceListRecursive(int[] descriptionTypeNidPreferenceList) {
        this.descriptionTypeSpecPreferenceList = ArrayUtil.toSpecificationArray(descriptionTypeNidPreferenceList);
        if (getNextProrityLanguageCoordinate().isPresent()) {
            ((LanguageCoordinateImpl) getNextProrityLanguageCoordinate().get()).setDescriptionTypePreferenceListRecursive(descriptionTypeNidPreferenceList);
        }
    }

    public void setDescriptionTypePreferenceListRecursive(ConceptSpecification[] descriptionTypeSpecPreferenceList) {
        this.descriptionTypeSpecPreferenceList = descriptionTypeSpecPreferenceList;
        if (getNextProrityLanguageCoordinate().isPresent()) {
            ((LanguageCoordinateImpl) getNextProrityLanguageCoordinate().get()).setDescriptionTypePreferenceListRecursive(descriptionTypeSpecPreferenceList);
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the dialect assemblage preference list.
     *
     * @return the dialect assemblage preference list
     */
    @Override
    public int[] getDialectAssemblagePreferenceList() {
        return ArrayUtil.toNidArray(this.dialectAssemblageSpecPreferenceList);
    }

    public void setDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceNidList) {
        this.dialectAssemblageSpecPreferenceList = ArrayUtil.toSpecificationArray(dialectAssemblagePreferenceNidList);
        altDescriptionTypeListCache.clear();
    }

    public void setLanguageConceptNid(int languageConceptNid) {
        this.languageConcept = Get.conceptSpecification(languageConceptNid);
    }

    public void setLanguageConcept(ConceptSpecification languageConcept) {
        this.languageConcept = languageConcept;
    }

    @Override
    @XmlElement(name = "Concept", type=ConceptProxy.class)
    @XmlElementWrapper(name = "dialectPreference") 
    public ConceptSpecification[] getDialectAssemblageSpecPreferenceList() {
        return dialectAssemblageSpecPreferenceList;
    }
    
    public void setDialectAssemblageSpecPreferenceList(ConceptSpecification[] dialectAssemblageSpecPreferenceList) {
        this.dialectAssemblageSpecPreferenceList = dialectAssemblageSpecPreferenceList;
    }
    
    @Override
    @XmlElement(name = "Concept", type=ConceptProxy.class)
    @XmlElementWrapper(name = "typePreference") 
    public ConceptSpecification[] getDescriptionTypeSpecPreferenceList() {
        return descriptionTypeSpecPreferenceList;
    }
    public void setDescriptionTypeSpecPreferenceList(ConceptSpecification[] descriptionTypeSpecPreferenceList) {
        this.descriptionTypeSpecPreferenceList = descriptionTypeSpecPreferenceList;
    }

    @Override
    @XmlElement(name = "Concept", type=ConceptProxy.class)
    @XmlElementWrapper(name = "modulePreference") 
    public ConceptSpecification[] getModuleSpecPreferenceListForLanguage() {
        return moduleSpecPreferenceList;
    }

    public void setModuleSpecPreferenceListForLanguage(ConceptSpecification[] moduleSpecPreferenceList) {
        this.moduleSpecPreferenceList = moduleSpecPreferenceList;
    }

    @Override
    public LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
            List<SemanticChronology> descriptionList,
            StampCoordinate stampCoordinate) {
        return getDescription(descriptionList, new int[]{TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()}, stampCoordinate);
    }

    /**
     * Gets the language concept nid.
     *
     * @return the language concept nid
     */
    @Override
    public int getLanguageConceptNid() {
        return this.languageConcept.getNid();
    }

    public ChangeListener<ObservableLanguageCoordinate> setNextProrityLanguageCoordinateProperty(
            ObjectProperty<ObservableLanguageCoordinate> nextProrityLanguageCoordinateProperty) {

        final ChangeListener<ObservableLanguageCoordinate> listener = (ObservableValue<? extends ObservableLanguageCoordinate> observable,
                ObservableLanguageCoordinate oldValue,
                ObservableLanguageCoordinate newValue) -> {
            this.nextProrityLanguageCoordinate = ((ObservableLanguageCoordinateImpl) newValue).unwrap();
        };

        nextProrityLanguageCoordinateProperty.addListener(new WeakChangeListener<>(listener));
        altDescriptionTypeListCache.clear();
        return listener;
    }

    @Override
    public LatestVersion<DescriptionVersion> getPreferredDescription(
            List<SemanticChronology> descriptionList,
            StampCoordinate stampCoordinate) {
        return getDescription(descriptionList, new int[]{TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()}, stampCoordinate);
    }

    @Override
    public LatestVersion<DescriptionVersion> getDefinitionDescription(
            List<SemanticChronology> descriptionList,
            StampCoordinate stampCoordinate) {
        return getDescription(descriptionList, new int[]{TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()}, stampCoordinate);
    }

    @Override
    public LanguageCoordinateImpl deepClone() {
        LanguageCoordinateImpl newCoordinate = new LanguageCoordinateImpl(languageConcept,
                dialectAssemblageSpecPreferenceList.clone(),
                descriptionTypeSpecPreferenceList.clone(),
                moduleSpecPreferenceList == null ? null : moduleSpecPreferenceList.clone());
        if (this.nextProrityLanguageCoordinate != null) {
            newCoordinate.nextProrityLanguageCoordinate = (LanguageCoordinateImpl) this.nextProrityLanguageCoordinate.deepClone();
        }
        return newCoordinate;
    }

    @Override
    public Optional<LanguageCoordinate> getNextProrityLanguageCoordinate() {
        return Optional.ofNullable(this.nextProrityLanguageCoordinate);
    }

    public void setNextProrityLanguageCoordinate(LanguageCoordinate languageCoordinate) {
        this.nextProrityLanguageCoordinate = (LanguageCoordinateImpl) languageCoordinate;
        altDescriptionTypeListCache.clear();
    }

    @Override
    public int[] getModulePreferenceListForLanguage() {
        return ArrayUtil.toNidArray(moduleSpecPreferenceList);
    }

    /**
     * Clone this coordinate, and change the description types list to the new
     * list, recursively. Also expands description types
     *
     * @param descriptionTypes
     * @return
     */
    private LanguageCoordinate cloneAndChangeDescriptionType(int[] descriptionTypes) {
        LanguageCoordinateImpl lci = deepClone();
        lci.setDescriptionTypePreferenceListRecursive(
                LanguageCoordinates.expandDescriptionTypePreferenceList(
                        ArrayUtil.toSpecificationArray(descriptionTypes),
                null));
        return lci;
    }
}
