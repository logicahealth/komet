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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.function.Supplier;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.collections.IntSet;

import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.tree.TreeNodeVisitData;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface TaxonomyService.
 * 
 * Many of the calls that use a manifold coordinate create a taxonomy tree in the background, 
 * and do not cache the results. So they are deprecated for now in favor of moving all these
 * methods to getSnapshot(). 
 * 
 *
 * @author kec
 */
@Contract
public interface TaxonomyService
        extends DatastoreServices {
   /**
    * Update the taxonomy by extracting relationships from the logical
    * definitions in the {@code logicGraphChronology}. This method will be
    * called by a commit listener, so developers do not have to update the
    * taxonomy themselves, unless developing an alternative taxonomy service
    * implementation.
    *
    * @param logicGraphChronology Chronology of the logical definitions
    */
   void updateTaxonomy(SemanticChronology logicGraphChronology);

   /**
    * Method to determine if a concept was ever a kind of another, without
 knowing a ManifoldCoordinate.
    *
    * @param childId a concept sequence or nid for the child concept
    * @param parentId a concept sequence or nid for the parent concept
    * @return true if child was ever a kind of the parent.
    */
   boolean wasEverKindOf(int childId, int parentId);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the all relationship origin sequences of type.
    *
    * @param destinationId the destination id
    * @param typeSequenceSet the type sequence set
    * @return the all relationship origin sequences of type
    */
   IntStream getAllRelationshipOriginNidsOfType(int destinationId, IntSet typeSequenceSet);

   /**
    * Gets the snapshot.
    *
    * @param tc the tc
    * @return the snapshot
    */
   TaxonomySnapshotService getSnapshot(ManifoldCoordinate tc);
   
   /**
    * 
    * @param conceptAssemblageNid The assemblage Nid which specifies the assemblage where the concepts in this tree
    * where created within.  
    * @return an unprocessed TreeNodeVisitData object. 
    */
   Supplier<TreeNodeVisitData> getTreeNodeVisitDataSupplier(int conceptAssemblageNid);
   
   
   /**
    * Due to the use of Weak References in the implementation, you MUST maintain a reference to the change listener that is passed in here,
    * otherwise, it will be rapidly garbage collected, and you will randomly stop getting change notifications!.
    * <br>
    * Notification occurs on the FxApplication thread. 
    * @param refreshListener the refresh listener
    */
   void addTaxonomyRefreshListener(RefreshListener refreshListener);
   
   /**
    * Called by processes when the process decides that it has concluded a set of changes, and taxonomy listeners should
    * refresh. This method will not send notifications for every change to the taxonomy (it will not notify when
    * updateTaxonomy(SemanticChronology logicGraphChronology) is called). If listeners want to be notified of 
    * every taxonomy change, they should use the addChangeListener(ChronologyChangeListener changeListener) 
    * capability provided the the CommitService, and filter the changes of interest to them. 
    * <br>
    * Notification occurs on the FxApplication thread, but this method may be called on any thread. 
    */
   void notifyTaxonomyListenersToRefresh();
   
}

