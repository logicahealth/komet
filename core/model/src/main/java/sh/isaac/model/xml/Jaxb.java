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
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.LetItemKey;
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
import sh.isaac.api.query.clauses.RelRestriction;
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
            jc = JAXBContext.newInstance(
                    StampCoordinateImpl.class,
                    LanguageCoordinateImpl.class,
                    LogicCoordinateImpl.class,
                    ConceptSpecification.class,
                    ConceptProxy.class, 
                    JaxbMap.class, 
                    Query.class,
                    Clause.class, 
                    Or.class,
                    And.class,
                    AndNot.class,
                    LeafClause.class,
                    Not.class,
                    Xor.class,
                    ChangedBetweenVersions.class,
                    ComponentIsActive.class,
                    ConceptIs.class,
                    ConceptIsChildOf.class,
                    ConceptIsDescendentOf.class,
                    ConceptIsKindOf.class,
                    DescriptionLuceneMatch.class,
                    DescriptionRegexMatch.class,
                    RelRestriction.class,
                    LetItemKey.class,
                    AttributeSpecification.class,
                    StampPrecedence.class,
                    Status.class,
                    StatusEnumSetAdaptor.class
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
