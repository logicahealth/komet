/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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

import java.util.stream.Stream;

/**
 * Computed results of an isomorphic comparison of two expressions: the 
 * reference expression and the comparison expression.
 * @author kec
 */
public interface IsomorphicResults {

    /**
     * 
     * @return roots for nodes that are in the reference expression, but not in the 
     * common expression. 
     */
    Stream<Node> getAdditionRoots();

    /**
     * 
     * @return an expression containing only the connected set of nodes representing
     * the maximal common isomorphism between the two expressions that are connected
     * to their respective roots. 
     */
    LogicalExpression getCommon();
    
    /**
     * 
     * @return roots for nodes that are in the comparison expression, but are not in 
     * the common expression. 
     */
    Stream<Node> getDeletionRoots();
    
}
