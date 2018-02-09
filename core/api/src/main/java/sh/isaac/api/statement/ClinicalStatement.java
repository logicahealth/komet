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

import java.util.Collection;
import java.util.UUID;
import sh.isaac.api.logic.LogicalExpression;

/**
 *
 * @author kec
 */
public interface ClinicalStatement {
    /**
     * 
     * @return measure of the time during which this clinical 
     * statement was recorded. 
     */
    Measure getStatmentTime();

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
    Collection<? extends Participant> getStatementAuthors();

    /**
     *
     * @return an expression of the subject of information (subject of record, mother of subject of record, etc.).
     */
    LogicalExpression getSubjectOfInformation();

    /**
     * The statement type must be consistent with the circumstance class.
     * Statements are generally either "request" or "performance"
     * @return the type of statement.
     */
    LogicalExpression getStatementType();

    /**
     *
     * @return the topic of this clinical statement.
     */
    LogicalExpression getTopic();

    /**
     *
     * @return the circumstances associated with this statement about the topic.
     */
    Circumstance getCircumstance();

    /**
     *
     * @return a collection of associations to other clinical statements, including panel parts.
     */
    Collection<StatementAssociation> getStatementAssociations();
}
