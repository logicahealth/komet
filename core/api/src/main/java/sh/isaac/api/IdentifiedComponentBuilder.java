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

import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.api.chronicle.Chronology;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface IdentifiedComponentBuilder.
 *
 * @author kec
 * @param <T> the generic type
 */
public interface IdentifiedComponentBuilder<T extends CommittableComponent>
        extends IdentifiedObject {
   /**
    * Add a nested Sememe that should be chained / built when build is called on this component.
    *
    * @param sememeBuilder the sememe builder
    * @return this object
    */
   public IdentifiedComponentBuilder<T> addSememe(SememeBuilder<?> sememeBuilder);

   /**
    * Add additional uuids as identifiers for this component.
    * @param uuids the additional uuids to add as identifiers for this component
    * @return  the builder for chaining of operations in a fluent pattern.
    */
   IdentifiedComponentBuilder<T> addUuids(UUID... uuids);

   /**
    * Create a component with a state of ACTIVE.
    *
    * @param editCoordinate the edit coordinate that determines the author, module and path for the change
    * @param changeCheckerMode determines if added to the commit manager with or without checks.
    * @return a task which will return the constructed component after it has been added to the commit manager -
    * the write to the commit manager is not complete until the task is complete (the task has already been launched)
    * @throws IllegalStateException the illegal state exception
    */
   OptionalWaitTask<T> build(EditCoordinate editCoordinate,
                             ChangeCheckerMode changeCheckerMode)
            throws IllegalStateException;

   /**
    * The caller is responsible to write the component to the proper store when
    * all updates to the component are complete.
    *
    * @param stampSequence the stamp sequence
    * @param builtObjects a list objects build as a result of call build.
    * Includes top-level object being built.
    * The caller is also responsible to write all build objects to the proper store.
    * @return the constructed component, not yet written to the database.
    * @throws IllegalStateException the illegal state exception
    */
   T build(int stampSequence,
           List<Chronology<? extends StampedVersion>> builtObjects)
            throws IllegalStateException;

   /**
    * Create a component with a state of ACTIVE.
    *
    * @param editCoordinate the edit coordinate that determines the author, module and path for the change
    * @param changeCheckerMode determines if added to the commit manager with or without checks.
    * @param subordinateBuiltObjects a list of subordinate objects also build as a result of building this object.  Includes top-level object being built.
    * @return a task which will return the constructed component after it has been added to the commit manager -
    * the write to the commit manager is not complete until the task is complete (the task has already been launched)
    * @throws IllegalStateException the illegal state exception
    */
   OptionalWaitTask<T> build(EditCoordinate editCoordinate,
                             ChangeCheckerMode changeCheckerMode,
                             List<Chronology<? extends StampedVersion>> subordinateBuiltObjects)
            throws IllegalStateException;

   //~--- set methods ---------------------------------------------------------

   /**
    * Set identifier for authority.
    *
    * @param identifier a string identifier such as a SNOMED CT id, or a LOINC id.
    * @param identifierAuthority a concept that identifies the authority that assigns the identifier.
    * @return the builder for chaining of operations in a fluent pattern.
    */
   IdentifiedComponentBuilder<T> setIdentifierForAuthority(String identifier, ConceptProxy identifierAuthority);

   /**
    * If not set, a randomly generated UUID will be automatically used.
    * @param uuidString the primordial uuid for the component to be built.
    * @return the builder for chaining of operations in a fluent pattern.
    */
   default IdentifiedComponentBuilder<T> setPrimordialUuid(String uuidString) {
      return setPrimordialUuid(UUID.fromString(uuidString));
   }

   /**
    * If not set, a randomly generated UUID will be automatically used.
    * @param uuid the primordial uuid for the component to be built.
    * @return the builder for chaining of operations in a fluent pattern.
    */
   IdentifiedComponentBuilder<T> setPrimordialUuid(UUID uuid);

   /**
    * define the state that the component will be created with.  if setState is not called,
    * the component will be build as active.  Note, this will not impact any nested builders.
    * Nested builders should have their own state set, if you wish to override the default
    * active value.  This is only used for calls to {@link #build(EditCoordinate, ChangeCheckerMode)}
    * or {@link #build(EditCoordinate, ChangeCheckerMode, List)} (where a active state would otherwise be assumed)
    * It is not used with a call to {@link #build(int, List)}
    *
    * @param state the state
    * @return the builder for chaining of operations in a fluent pattern.
    */
   IdentifiedComponentBuilder<T> setState(State state);
}

