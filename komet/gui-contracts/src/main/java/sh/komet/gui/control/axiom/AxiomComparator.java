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
package sh.komet.gui.control.axiom;

import java.util.Comparator;
import sh.isaac.MetaData;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.komet.gui.control.axiom.AxiomView.ClauseView;

/**
 *
 * @author kec
 */
public class AxiomComparator implements Comparator<ClauseView> {

    @Override
    public int compare(ClauseView o1, ClauseView o2) {
        if (!o1.logicNode.getClass().equals(o2.logicNode.getClass())) {
            if (o1.logicNode instanceof SufficientSetNode) {
                return -1;
            }
            if (o2.logicNode instanceof SufficientSetNode) {
                return 1;
            }
            if (o1.logicNode instanceof NecessarySetNode) {
                return -1;
            }
            if (o2.logicNode instanceof NecessarySetNode) {
                return 1;
            }
            if (o1.logicNode instanceof ConceptNodeWithNids) {
                return -1;
            }
            if (o2.logicNode instanceof ConceptNodeWithNids) {
                return 1;
            }
            if (o1.logicNode instanceof FeatureNodeWithNids) {
                return -1;
            }
            if (o2.logicNode instanceof FeatureNodeWithNids) {
                return 1;
            }
            if (o1.logicNode instanceof RoleNodeSomeWithNids &&
                    ((RoleNodeSomeWithNids) o1.logicNode).getTypeConceptNid() != MetaData.ROLE_GROUP____SOLOR.getNid()) {
                return -1;
            }
            if (o2.logicNode instanceof RoleNodeSomeWithNids &&
                    ((RoleNodeSomeWithNids) o2.logicNode).getTypeConceptNid() != MetaData.ROLE_GROUP____SOLOR.getNid()) {
                return 1;
            }
            if (o1.logicNode instanceof RoleNodeAllWithNids) {
                return -1;
            }
            if (o2.logicNode instanceof RoleNodeAllWithNids) {
                return 1;
            }
            if (o1.logicNode instanceof RoleNodeSomeWithNids &&
                    ((RoleNodeSomeWithNids) o1.logicNode).getTypeConceptNid() == MetaData.ROLE_GROUP____SOLOR.getNid()) {
                return -1;
            }
            if (o2.logicNode instanceof RoleNodeSomeWithNids &&
                    ((RoleNodeSomeWithNids) o2.logicNode).getTypeConceptNid() == MetaData.ROLE_GROUP____SOLOR.getNid()) {
                return 1;
            }
            throw new UnsupportedOperationException("Can't sort: " + o1.logicNode.getClass().getName());
        }
            if (o1.logicNode instanceof RoleNodeSomeWithNids &&
                    ((RoleNodeSomeWithNids) o1.logicNode).getTypeConceptNid() != MetaData.ROLE_GROUP____SOLOR.getNid()&&
                    ((RoleNodeSomeWithNids) o2.logicNode).getTypeConceptNid() == MetaData.ROLE_GROUP____SOLOR.getNid()) {
                return -1;
            }
            if (o2.logicNode instanceof RoleNodeSomeWithNids &&
                    ((RoleNodeSomeWithNids) o2.logicNode).getTypeConceptNid() != MetaData.ROLE_GROUP____SOLOR.getNid() &&
                    ((RoleNodeSomeWithNids) o1.logicNode).getTypeConceptNid() == MetaData.ROLE_GROUP____SOLOR.getNid()) {
                return 1;
            }
        return o1.titleLabel.getText().compareTo(o2.titleLabel.getText());
    }
    
}
