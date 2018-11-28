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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.Query;

/**
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ReferencedComponentIsNotKindOf
     extends ReferencedComponentIsKindOf {

    public ReferencedComponentIsNotKindOf() {
    }

    public ReferencedComponentIsNotKindOf(Query enclosingQuery, LetItemKey parentSpecKey, LetItemKey manifoldCoordinateKey) {
        super(enclosingQuery, parentSpecKey, manifoldCoordinateKey);
    }

    @Override
    protected boolean test(TaxonomySnapshot snapshot, int childNid, int parentNid) {
        return !super.test(snapshot, childNid, parentNid);
    }
    
}
