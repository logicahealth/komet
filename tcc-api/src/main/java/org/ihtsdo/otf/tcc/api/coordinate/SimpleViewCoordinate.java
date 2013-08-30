/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.api.coordinate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerPolicy;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.spec.SimpleConceptSpecification;

/**
 * A view coordinate that is simplified for JAXB marshall/unmarshall.
 * @author kec
 */
@XmlRootElement(name = "simple-view-coordinate")
public class SimpleViewCoordinate {
    private SimpleConceptSpecification classifierSpecification;
    private SimpleConceptSpecification languageSpecification;
    private List<SimpleConceptSpecification> languagePreferenceOrderList = new ArrayList<>();
    private EnumSet<Status> allowedStatus = EnumSet.noneOf(Status.class);
    private Precedence precedence;
    private RelAssertionType relAssertionType;
    private String name;
    private SimplePosition viewPosition;
    private UUID coordinateUuid = UUID.randomUUID();
    private ContradictionManagerPolicy contradictionPolicy = ContradictionManagerPolicy.IDENTIFY_ALL_CONFLICTS;
    private LanguageSort langSort = LanguageSort.RF2_LANG_REFEX;

    public LanguageSort getLangSort() {
        return langSort;
    }

    public void setLangSort(LanguageSort langSort) {
        this.langSort = langSort;
    }

    public ContradictionManagerPolicy getContradictionPolicy() {
        return contradictionPolicy;
    }

    public void setContradictionPolicy(ContradictionManagerPolicy contradictionPolicy) {
        this.contradictionPolicy = contradictionPolicy;
    }

    public UUID getCoordinateUuid() {
        return coordinateUuid;
    }

    public void setCoordinateUuid(UUID coordinateUuid) {
        this.coordinateUuid = coordinateUuid;
    }

    public SimpleConceptSpecification getClassifierSpecification() {
        return classifierSpecification;
    }

    public void setClassifierSpecification(SimpleConceptSpecification classifierSpecification) {
        this.classifierSpecification = classifierSpecification;
    }

    public SimpleConceptSpecification getLanguageSpecification() {
        return languageSpecification;
    }

    public void setLanguageSpecification(SimpleConceptSpecification languageSpecification) {
        this.languageSpecification = languageSpecification;
    }

    public List<SimpleConceptSpecification> getLanguagePreferenceOrderList() {
        return languagePreferenceOrderList;
    }

    public void setLanguagePreferenceOrderList(List<SimpleConceptSpecification> languagePreferenceOrderList) {
        this.languagePreferenceOrderList = languagePreferenceOrderList;
    }

    public EnumSet<Status> getAllowedStatus() {
        return allowedStatus;
    }

    public void setAllowedStatus(EnumSet<Status> allowedStatus) {
        this.allowedStatus = allowedStatus;
    }

    public Precedence getPrecedence() {
        return precedence;
    }

    public void setPrecedence(Precedence precedence) {
        this.precedence = precedence;
    }

    public RelAssertionType getRelAssertionType() {
        return relAssertionType;
    }

    public void setRelAssertionType(RelAssertionType relAssertionType) {
        this.relAssertionType = relAssertionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimplePosition getViewPosition() {
        return viewPosition;
    }

    public void setViewPosition(SimplePosition viewPosition) {
        this.viewPosition = viewPosition;
    }
}
