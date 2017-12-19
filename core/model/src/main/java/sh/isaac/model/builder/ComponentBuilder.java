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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.Status;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.api.component.semantic.SemanticBuilder;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ComponentBuilder.
 *
 * @author kec
 * @param <T> the generic type
 */
public abstract class ComponentBuilder<T extends CommittableComponent>
         implements IdentifiedComponentBuilder<T> {
   /** The additional uuids. */
   protected final List<UUID> additionalUuids = new ArrayList<>();

   /** The primordial uuid. */
   private UUID primordialUuid = null;

   /** The sememe builders. */
   protected final List<SemanticBuilder<?>> semanticBuilders = new ArrayList<>();

   /** The state. */
   protected Status state = Status.ACTIVE;

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the sememe.
    *
    * @param semanticBuilder the sememe builder
    * @return the component builder
    */
   @Override
   public ComponentBuilder<T> addSemantic(SemanticBuilder<?> semanticBuilder) {
      this.semanticBuilders.add(semanticBuilder);
      return this;
   }

   /**
    * Adds the uuids.
    *
    * @param uuids the uuids
    * @return the identified component builder
    */
   @Override
   public IdentifiedComponentBuilder<T> addUuids(UUID... uuids) {
      if (uuids != null) {
         for (final UUID uuid: uuids) {
            if (!uuid.equals(this.primordialUuid)) {
               this.additionalUuids.add(uuid);
            }
         }
      }

      return this;
   }

   /**
    * Builds the.
    *
    * @param editCoordinate the edit coordinate
    * @param changeCheckerMode the change checker mode
    * @return the optional wait task
    * @throws IllegalStateException the illegal state exception
    */
   @Override
   public final OptionalWaitTask<T> build(EditCoordinate editCoordinate,
         ChangeCheckerMode changeCheckerMode)
            throws IllegalStateException {
      return build(editCoordinate, changeCheckerMode, new ArrayList<>());
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set identifier for authority.
    *
    * @param identifier the identifier
    * @param identifierAuthority the identifier authority
    * @return the identified component builder
    */
   @Override
   public IdentifiedComponentBuilder<T> setIdentifierForAuthority(String identifier, ConceptProxy identifierAuthority) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return Get.identifierService()
                .getNidForUuids(getUuids());
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
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
    *
    * @param uuid the uuid
    * @return the builder for chaining of operations in a fluent pattern.
    */
   @Override
   public IdentifiedComponentBuilder<T> setPrimordialUuid(UUID uuid) {
      this.primordialUuid = uuid;
      return this;
   }

   /**
    * Set state.
    *
    * @param state the state
    * @return the identified component builder
    */
   @Override
   public IdentifiedComponentBuilder<T> setState(Status state) {
      this.state = state;
      return this;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() { // TODO use list instead of stream
      return Arrays.asList(getUuids());
   }

   /**
    * Gets the uuids.
    *
    * @return the uuids
    */
   @Override
   public UUID[] getUuids() { 
      if (this.additionalUuids.isEmpty()) {
         return new UUID[] { getPrimordialUuid() };
      }
      UUID[] uuids = new UUID[this.additionalUuids.size() + 1];
      uuids[0] = getPrimordialUuid();
      
      for (int i = 0; i < this.additionalUuids.size(); i++) {
         uuids[i+1] = this.additionalUuids.get(i);
      }
      return uuids;
   }
}

