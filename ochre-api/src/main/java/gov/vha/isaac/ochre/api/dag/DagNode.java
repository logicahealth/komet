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
 * @param <T>
 */
public class DagNode<T> {
    private final T data;

    public T getData() {
        return data;
    }

    public Graph<T> getGraph() {
        return graph;
    }
    private final DagNode<T> parent;
    private final Graph<T> graph;
    private final List<DagNode<T>> children = new ArrayList<>();

    public DagNode(T data, Graph<T> graph) {
        this.data = data;
        this.parent = null;
        this.graph = graph;
    }

    public DagNode(T data, DagNode<T> parent) {
        this.data = data;
        this.parent = parent;
        this.graph = parent.graph;
    }

    public final DagNode<T> addChild(T t) {
        DagNode<T> child = new DagNode<>(t, this);
        children.add(child);
        this.graph.setLastAddedNode(child);
        return child;
    }
    
    public List<DagNode<T>> getChildren() {
        return children;
    }

    public DagNode<T> getParent() {
        return parent;
    }
    
}
