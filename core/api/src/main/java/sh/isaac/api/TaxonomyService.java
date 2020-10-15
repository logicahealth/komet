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

import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.navigation.NavigationRecord;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;

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
    * Return the stored taxonomy data for the specified concept in the given assemblage.
    * @param assemblageNid The assemblage to read the data from
    * @param conceptNid The concept within the assemblage to read the data from
    * @return The taxonomy data
    */
   int[] getTaxonomyData(int assemblageNid, int conceptNid);

   NavigationRecord getNavigationRecord(int conceptNid);
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
    * Gets the all relationship origin concept nids of type.
    *
    * @param destinationConceptNid the destination id
    * @param typeConceptNidSet the type nids set
    * @return the all relationship origin nids of type
    */
   IntStream getAllRelationshipOriginNidsOfType(int destinationConceptNid, IntSet typeConceptNidSet);

   /**
    * Gets the snapshot.  This method is for returning a Snapshot that builds an entire tree in a background thread.
    * The returned {@link TaxonomySnapshot} can be used immediately, while it computes in the background - until
    * the entire tree is computed, it will answer queries via direct lookups.  After the tree is computed, it will use
    * the cache to answer queries.  This approach is best for a use case where the TaxonomySnapshotService will be used
    * for many queries for a period of time.
    *
    * @param mc the manifold coordinate
    * @return the snapshot which is backed by a {@link Tree}, although that tree may not be complete for some time after
    * this call returns.
    */
   TaxonomySnapshot getSnapshot(ManifoldCoordinate mc);

   /**
    * Gets the snapshot.  This method is for returning a Snapshot that does NOT build a tree in the background.
    * Every query will be answered by direct computation on the call.  Implementations may do some caching of answers
    * previously computed.  
    * 
    * This approach is best for a use case where the TaxonomySnapshotService will be used to answer a single query, and 
    * then be thrown away.
    *
    * @param mc the manifold coordinate
    * @return the snapshot that is NOT backed by a {@link Tree}
    */
   TaxonomySnapshot getSnapshotNoTree(ManifoldCoordinate mc);

    /**
     * Calls {@link #getSnapshot(ManifoldCoordinate)} with a manifold constructed from the provided path,
     * modules, and states.  Uses {@link PremiseType#STATED} and a time of MAX_VALUE.  Language is set to the 
     * system default.
     * @param pathNid
     * @param modules
     * @param allowedStates
     * @param computeTree true, if this should call {@link #getSnapshot(ManifoldCoordinate)}, false if it should call
     * {@link #getSnapshotNoTree(ManifoldCoordinate)}
     * @return the Snapshot service
     */
   TaxonomySnapshot getStatedLatestSnapshot(int pathNid, Set<ConceptSpecification> modules, Set<Status> allowedStates, boolean computeTree);

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
   
   /**
    * Atomically updates the element at index {@code conceptNid} with the  results of applying the given function 
    * to the current and given values, returning the updated value. The function should  be side-effect-free, since 
    * it may be re-applied when attempted updates fail due to contention among threads.  The function is applied with 
    * the current value at index {@code conceptNid} as its first  argument, and the given update as the second argument.
    * @param assemblageNid The assemblage to read the data from
    * @param conceptNid The concept within the assemblage to read the data from, and to store the new data on
    * @param newData The new value
    * @param accumulatorFunction The function to merge the old and new values
    * @return The new, merged value.
    */
   int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction);
   
   /**
    * Checks if kindOf, ignoring all coordinates (active, inactive, any path, any module, etc) 
    * @param childNid
    * @param parentNid
    * @return
    */
   public boolean wasEverKindOf(int childNid, int parentNid);
   
   /**
    * Checks if childOf, ignoring all coordinates (active, inactive, any path, any module, etc)
    * @param childNid
    * @param parentNid
    * @return
    */
   public boolean wasEverChildOf(int childNid, int parentNid);
   
   /**
    * Gets isA children of the specified concept, ignoring all coordinates (active, inactive, any path, any module)
    * @param parentNid
    * @return
    */
   public int[] getAllTaxonomyChildren(int parentNid);
}

