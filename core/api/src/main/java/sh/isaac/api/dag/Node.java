/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.dag;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <T>
 */
public class Node<T> {
   private final List<Node<T>> children = new ArrayList<>();
   private final T             data;
   private final Node<T>       parent;
   private final Graph<T>      graph;

   //~--- constructors --------------------------------------------------------

   public Node(T data, Graph<T> graph) {
      this.data   = data;
      this.parent = null;
      this.graph  = graph;
   }

   public Node(T data, Node<T> parent) {
      this.data   = data;
      this.parent = parent;
      this.graph  = parent.graph;
   }

   //~--- methods -------------------------------------------------------------

   public final Node<T> addChild(T t) {
      Node<T> child = new Node<>(t, this);

      children.add(child);
      this.graph.setLastAddedNode(child);
      return child;
   }

   //~--- get methods ---------------------------------------------------------

   public List<Node<T>> getChildren() {
      return children;
   }

   public T getData() {
      return data;
   }

   public Graph<T> getGraph() {
      return graph;
   }

   public Node<T> getParent() {
      return parent;
   }
}

