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



package sh.isaac.model.builder;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.State;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.task.OptionalWaitTask;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <T>
 */
public abstract class ComponentBuilder<T extends CommittableComponent>
         implements IdentifiedComponentBuilder<T> {
   protected final List<UUID>             additionalUuids = new ArrayList<>();
   private UUID                           primordialUuid  = null;
   protected final List<SememeBuilder<?>> sememeBuilders  = new ArrayList<>();
   protected State                        state           = State.ACTIVE;

   //~--- methods -------------------------------------------------------------

   @Override
   public ComponentBuilder<T> addSememe(SememeBuilder<?> sememeBuilder) {
      sememeBuilders.add(sememeBuilder);
      return this;
   }

   @Override
   public IdentifiedComponentBuilder<T> addUuids(UUID... uuids) {
      if (uuids != null) {
         for (UUID uuid: uuids) {
            if (!uuid.equals(primordialUuid)) {
               additionalUuids.add(uuid);
            }
         }
      }

      return this;
   }

   @Override
   public final OptionalWaitTask<T> build(EditCoordinate editCoordinate,
         ChangeCheckerMode changeCheckerMode)
            throws IllegalStateException {
      return build(editCoordinate, changeCheckerMode, new ArrayList<>());
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public IdentifiedComponentBuilder<T> setIdentifierForAuthority(String identifier, ConceptProxy identifierAuthority) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid() {
      return Get.identifierService()
                .getNidForUuids(getUuids());
   }

   @Override
   public UUID getPrimordialUuid() {
      if (this.primordialUuid == null) {
         this.primordialUuid = UUID.randomUUID();  // This is a slow operation - lazy load.
      }

      return this.primordialUuid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * If not set, a randomly generated UUID will be automatically used.
    * @param uuid
    * @return the builder for chaining of operations in a fluent pattern.
    */
   @Override
   public IdentifiedComponentBuilder<T> setPrimordialUuid(UUID uuid) {
      this.primordialUuid = uuid;
      return this;
   }

   @Override
   public IdentifiedComponentBuilder<T> setState(State state) {
      this.state = state;
      return this;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<UUID> getUuidList() {
      Stream.Builder<UUID> builder = Stream.builder();

      builder.accept(getPrimordialUuid());
      additionalUuids.forEach((uuid) -> builder.accept(uuid));
      return builder.build()
                    .collect(Collectors.toList());
   }

   @Override
   public UUID[] getUuids() {
      Stream.Builder<UUID> builder = Stream.builder();

      builder.accept(getPrimordialUuid());
      additionalUuids.forEach((uuid) -> builder.accept(uuid));
      return builder.build()
                    .toArray((int length) -> new UUID[length]);
   }
}

