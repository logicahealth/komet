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
package sh.isaac.model.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.query.And;
import sh.isaac.api.query.AndNot;
import sh.isaac.api.query.AttributeSpecification;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.Join;
import sh.isaac.api.query.JoinSpec;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.ManifoldCoordinateForQuery;
import sh.isaac.api.query.Not;
import sh.isaac.api.query.Or;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.Xor;
import sh.isaac.api.query.clauses.ChangedBetweenVersions;
import sh.isaac.api.query.clauses.ComponentIsActive;
import sh.isaac.api.query.clauses.ConceptIs;
import sh.isaac.api.query.clauses.ConceptIsChildOf;
import sh.isaac.api.query.clauses.ConceptIsDescendentOf;
import sh.isaac.api.query.clauses.ConceptIsKindOf;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.isaac.api.query.clauses.DescriptionRegexMatch;
import sh.isaac.api.query.clauses.ReferencedComponentIs;
import sh.isaac.api.query.clauses.ReferencedComponentIsActive;
import sh.isaac.api.query.clauses.ReferencedComponentIsKindOf;
import sh.isaac.api.query.clauses.ReferencedComponentIsMemberOf;
import sh.isaac.api.query.clauses.ReferencedComponentIsNotActive;
import sh.isaac.api.query.clauses.ReferencedComponentIsNotKindOf;
import sh.isaac.api.query.clauses.ReferencedComponentIsNotMemberOf;
import sh.isaac.api.query.clauses.RelRestriction;
import sh.isaac.api.query.clauses.SemanticContainsString;
import sh.isaac.api.xml.JaxbMap;
import sh.isaac.model.coordinate.LanguageCoordinateImpl;
import sh.isaac.model.coordinate.LogicCoordinateImpl;
import sh.isaac.model.coordinate.StampCoordinateImpl;

/**
 *
 * @author kec
 */
public class Jaxb {
    private static final Jaxb JAXB = new Jaxb();
    final JAXBContext jc;
    private Jaxb() {
        try {
            jc = JAXBContext.newInstance(And.class,
                    AndNot.class,
                    AttributeSpecification.class,
                    ChangedBetweenVersions.class,
                    Clause.class, 
                    ComponentIsActive.class,
                    ConceptProxy.class, 
                    ConceptSpecification.class,
                    ConceptIs.class,
                    ConceptIsChildOf.class,
                    ConceptIsDescendentOf.class,
                    ConceptIsKindOf.class,
                    DescriptionLuceneMatch.class,
                    DescriptionRegexMatch.class,
                    JaxbMap.class, 
                    Join.class,
                    JoinSpec.class,
                    LanguageCoordinateImpl.class,
                    LeafClause.class,
                    LetItemKey.class,
                    LogicCoordinateImpl.class,
                    ManifoldCoordinateForQuery.class,
                    Not.class,
                    Or.class,
                    Query.class,
                    RelRestriction.class,
                    ReferencedComponentIs.class,
                    ReferencedComponentIsActive.class,
                    ReferencedComponentIsKindOf.class,
                    ReferencedComponentIsMemberOf.class,
                    ReferencedComponentIsNotActive.class,
                    ReferencedComponentIsNotKindOf.class,
                    ReferencedComponentIsNotMemberOf.class,
                    SemanticContainsString.class,
                    StampCoordinateImpl.class,
                    StampPrecedence.class,
                    Status.class,
                    StatusEnumSetAdaptor.class,
                    Xor.class 
            );
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static Marshaller createMarshaller() throws JAXBException {
        return JAXB.jc.createMarshaller();
    }
    public static Unmarshaller createUnmarshaller() throws JAXBException {
        return JAXB.jc.createUnmarshaller();
    }
}
