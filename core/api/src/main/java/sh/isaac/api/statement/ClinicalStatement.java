/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.statement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.StampCoordinate;

/**
 *
 * @author kec
 */
public interface ClinicalStatement {
    
    /**
     * 
     * @return concept indicating if this is a template or an instance
     */
    ConceptChronology getMode();
    /**
     * 
     * @return the stamp coordinate for this clinical statement. 
     */
    StampCoordinate getStampCoordinate();
    /**
     * 
     * @return An optional narrative of this clinical statement. 
     */
    Optional<String> getNarrative();
    /**
     * 
     * @return measure of the time during which this clinical 
     * statement was recorded. 
     */
    Measure getStatementTime();

    /**
     *
     * @return a unique identifier for this clinical statement.
     */
    UUID getStatementId();

    /**
     *
     * @return a unique identifier for the subject of record.
     */
    UUID getSubjectOfRecordId();

    /**
     *
     * @return the authors of this statement.
     */
    List<? extends Participant> getStatementAuthors();

    /**
     *
     * @return an expression of the subject of information (subject of record, mother of subject of record, etc.).
     */
    ConceptChronology getSubjectOfInformation();

    /**
     * The statement type must be consistent with the circumstance class.
     * Statements are generally either "request" or "performance"
     * @return the type of statement.
     */
    ConceptChronology getStatementType();

    /**
     *
     * @return the topic of this clinical statement.
     */
    ConceptChronology getTopic();

    /**
     *
     * @return the circumstances associated with this statement about the topic.
     */
    Circumstance getCircumstance();

    /**
     *
     * @return a collection of associations to other clinical statements, including panel parts.
     */
    List<StatementAssociation> getStatementAssociations();
}
