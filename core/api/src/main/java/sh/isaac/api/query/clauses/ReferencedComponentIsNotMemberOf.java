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
package sh.isaac.api.query.clauses;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.Query;

/**
 *
 * @author kec
 */
public class ReferencedComponentIsNotMemberOf
        extends ReferencedComponentIsMemberOf {

    public ReferencedComponentIsNotMemberOf() {
    }

    public ReferencedComponentIsNotMemberOf(Query enclosingQuery, LetItemKey assemblageSpecKey, LetItemKey stampCoordinateKey) {
        super(enclosingQuery, assemblageSpecKey, stampCoordinateKey);
    }

    @Override
    protected boolean test(LatestVersion<SemanticVersion> latest) {
        return !super.test(latest); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.REFERENCED_COMPONENT_IS_NOT_MEMBER_OF; 
    }

}
