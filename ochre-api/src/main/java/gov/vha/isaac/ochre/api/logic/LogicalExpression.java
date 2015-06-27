/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.logic;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;


import java.util.function.BiConsumer;

/**
 *
 * @author kec
 */
public interface LogicalExpression {
    byte[][] getData(DataTarget dataTarget);
    
    boolean isMeaningful();

    int getConceptSequence();

    Node getNode(int nodeIndex);

    int getNodeCount();

    Node getRoot();

    byte[][] pack(DataTarget dataTarget);

    void processDepthFirst(BiConsumer<Node, TreeNodeVisitData> consumer);
}
