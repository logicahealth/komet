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
package gov.vha.isaac.ochre.api.dag;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kec
 * @param <T> The type of object enclosed by nodes in this graph. 
 */
public class Graph<T> {
    private final List<DagNode<T>> nodes = new ArrayList<>();
    private DagNode<T> root;
    private DagNode<T> lastAddedNode;

    public Graph() {    }
    
    public Graph(T rootData) {
        createRoot(rootData);
    }
    
   public final void createRoot(T rootData) {
        root = new DagNode<>(rootData, this);
        this.lastAddedNode = root;
        this.nodes.add(lastAddedNode);
   }
    
    public DagNode<T> getRoot() {
        return root;
    }
 
    
    public DagNode<T> getLastAddedNode() {
        return lastAddedNode;
    }

    public void setLastAddedNode(DagNode<T> lastAddedNode) {
        this.lastAddedNode = lastAddedNode;
        this.nodes.add(lastAddedNode);
    }

    

}
