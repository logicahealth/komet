/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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

import java.io.IOException;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerPolicy;
import org.ihtsdo.otf.tcc.api.contradiction.strategy.IdentifyAllConflict;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.spec.SimpleConceptSpecification;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author kec
 */
public class StandardViewCoordinates {


        
    public static ViewCoordinate getSnomedInferredLatest() throws IOException {
        ViewCoordinate snomedVc = new ViewCoordinate(UUID.fromString("0c734870-836a-11e2-9e96-0800200c9a66"),
                "SNOMED Infered-Latest", Ts.get().getMetadataVC());
        Position snomedPosition
                = Ts.get().newPosition(Ts.get().getPath(Snomed.SNOMED_RELEASE_PATH.getLenient().getConceptNid()),
                        Long.MAX_VALUE);

        snomedVc.setViewPosition(snomedPosition);
        snomedVc.setRelationshipAssertionType(RelAssertionType.INFERRED);
        snomedVc.setAllowedStatus(EnumSet.of(Status.ACTIVE, Status.INACTIVE));

        return snomedVc;
    }

    public static ViewCoordinate getSnomedInferredLatestActiveOnly() throws IOException {
        ViewCoordinate snomedVc = new ViewCoordinate(UUID.fromString("dbc50104-71d9-11e4-b116-123b93f75cba"),
                "SNOMED Infered-Latest", Ts.get().getMetadataVC());
        Position snomedPosition
                = Ts.get().newPosition(Ts.get().getPath(Snomed.SNOMED_RELEASE_PATH.getLenient().getConceptNid()),
                        Long.MAX_VALUE);

        snomedVc.setViewPosition(snomedPosition);
        snomedVc.setRelationshipAssertionType(RelAssertionType.INFERRED);
        snomedVc.setAllowedStatus(EnumSet.of(Status.ACTIVE));

        return snomedVc;
    }

    
    public static ViewCoordinate getSnomedStatedLatest() throws IOException {
        ViewCoordinate snomedVc = new ViewCoordinate(UUID.fromString("dbc50708-71d9-11e4-b116-123b93f75cba"),
                "SNOMED Stated-Latest", Ts.get().getMetadataVC());
        Position snomedPosition
                = Ts.get().newPosition(Ts.get().getPath(Snomed.SNOMED_RELEASE_PATH.getLenient().getConceptNid()),
                        Long.MAX_VALUE);

        snomedVc.setViewPosition(snomedPosition);
        snomedVc.setRelationshipAssertionType(RelAssertionType.STATED);

        return snomedVc;
    }

    
    public static ViewCoordinate getSnomedInferredLatestActiveAndInactive() throws IOException {
        ViewCoordinate snomedVc = new ViewCoordinate(UUID.fromString("dbc50bcc-71d9-11e4-b116-123b93f75cba"),
                "SNOMED Infered-Latest", Ts.get().getMetadataVC());
        Position snomedPosition
                = Ts.get().newPosition(Ts.get().getPath(Snomed.SNOMED_RELEASE_PATH.getLenient().getConceptNid()),
                        Long.MAX_VALUE);

        snomedVc.setViewPosition(snomedPosition);
        snomedVc.setRelationshipAssertionType(RelAssertionType.INFERRED);
        snomedVc.setAllowedStatus(EnumSet.of(Status.ACTIVE, Status.INACTIVE));

        return snomedVc;
    }

    public static ViewCoordinate getSnomedInferredThenStatedLatest() throws IOException {
        ViewCoordinate snomedVc = new ViewCoordinate(UUID.fromString("dbc50ec4-71d9-11e4-b116-123b93f75cba"),
                "SNOMED Inferred then Stated-Latest", Ts.get().getMetadataVC());
        Position snomedPosition
                = Ts.get().newPosition(Ts.get().getPath(Snomed.SNOMED_RELEASE_PATH.getLenient().getConceptNid()),
                        Long.MAX_VALUE);

        snomedVc.setViewPosition(snomedPosition);
        snomedVc.setRelationshipAssertionType(RelAssertionType.INFERRED_THEN_STATED);

        return snomedVc;
    }
    
    public static ViewCoordinate getMetadataViewCoordinate() throws IOException {
        Path viewPath = new Path(TermAux.WB_AUX_PATH.getNid(), null);
        Position viewPosition = new Position(Long.MAX_VALUE, viewPath);
        EnumSet<Status> allowedStatusNids = EnumSet.of(Status.ACTIVE);
        ContradictionManagerBI contradictionManager = new IdentifyAllConflict();
        int languageNid = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getNid();
        int classifierNid = TermAux.IHTSDO_CLASSIFIER.getNid();

        return new ViewCoordinate(UUID.fromString("dbc5119e-71d9-11e4-b116-123b93f75cba"), "meta-vc", Precedence.PATH,
                viewPosition, allowedStatusNids, contradictionManager, languageNid, classifierNid,
                RelAssertionType.INFERRED_THEN_STATED, null, LanguageSort.RF2_LANG_REFEX);
    }

    
    public static SimpleViewCoordinate previousVC(int year, int month, int day, int hour, int minute) {
        SimpleViewCoordinate svc = new SimpleViewCoordinate();
        svc.setName("Snomed Inferred Latest");
        svc.setClassifierSpecification(getSpec("IHTSDO Classifier",
                "7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
        svc.setLanguageSpecification(getSpec("United States of America English language reference set (foundation metadata concept)",
                "bca0a686-3516-3daf-8fcf-fe396d13cfad"));
        svc.getLanguagePreferenceOrderList().add(svc.getLanguageSpecification());
        svc.getAllowedStatus().add(Status.ACTIVE);
        svc.setPrecedence(Precedence.PATH);
        SimplePath wbAuxPath = new SimplePath();
        wbAuxPath.setPathConceptSpecification(getSpec("Workbench Auxiliary",
                "2faa9260-8fb2-11db-b606-0800200c9a66"));
        SimplePosition snomedWbAuxOrigin = new SimplePosition();
        snomedWbAuxOrigin.setPath(wbAuxPath);
        // Long.MAX_VALUE == latest
        snomedWbAuxOrigin.setTimePoint(Long.MAX_VALUE);

        SimplePath snomedCorePath = new SimplePath();
        snomedCorePath.setPathConceptSpecification(getSpec("SNOMED Core",
                "8c230474-9f11-30ce-9cad-185a96fd03a2"));
        snomedCorePath.getOrigins().add(snomedWbAuxOrigin);

        GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute);
        long time = calendar.getTimeInMillis();

        SimplePosition previousPosition = new SimplePosition();
        previousPosition.setPath(snomedCorePath);
        previousPosition.setTimePoint(time);
        svc.setViewPosition(previousPosition);

        svc.setRelAssertionType(RelAssertionType.INFERRED);
        svc.setContradictionPolicy(ContradictionManagerPolicy.LAST_COMMIT_WINS);
        svc.setLangSort(LanguageSort.RF2_LANG_REFEX);

        return svc;

    }

    private static SimpleConceptSpecification getSpec(String description, String uuidStr) {
        SimpleConceptSpecification classifierSpec = new SimpleConceptSpecification();
        classifierSpec.setDescription(description);
        classifierSpec.setUuid(uuidStr);
        return classifierSpec;
    }
}
