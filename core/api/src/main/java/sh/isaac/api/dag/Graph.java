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
 * The Class Graph.
 *
 * @author kec
 * @param <T> The type of object enclosed by nodes in this graph.
 */
public class Graph<T> {
   
   /** The nodes. */
   private final List<Node<T>> nodes = new ArrayList<>();
   
   /** The root. */
   private Node<T>             root;
   
   /** The last added node. */
   private Node<T>             lastAddedNode;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new graph.
    */
   public Graph() {}

   /**
    * Instantiates a new graph.
    *
    * @param rootData the root data
    */
   public Graph(T rootData) {
      createRoot(rootData);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Creates the root.
    *
    * @param rootData the root data
    */
   public final void createRoot(T rootData) {
      this.root               = new Node<>(rootData, this);
      this.lastAddedNode = this.root;
      this.nodes.add(this.lastAddedNode);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the last added node.
    *
    * @return the last added node
    */
   public Node<T> getLastAddedNode() {
      return this.lastAddedNode;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the last added node.
    *
    * @param lastAddedNode the new last added node
    */
   public void setLastAddedNode(Node<T> lastAddedNode) {
      this.lastAddedNode = lastAddedNode;
      this.nodes.add(lastAddedNode);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the root.
    *
    * @return the root
    */
   public Node<T> getRoot() {
      return this.root;
   }
}

