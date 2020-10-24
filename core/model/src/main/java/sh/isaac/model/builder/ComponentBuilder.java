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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.Status;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;

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
   
   protected final int assemblageId;

   /** The semantic builders. */
   private final List<SemanticBuilder<?>> semanticBuilders = new ArrayList<>();

   /** The state. */
   protected Status status = null;
   
   private Optional<Integer> module = Optional.empty();

   public ComponentBuilder(int assemblageId) {
      this.assemblageId = assemblageId;
   }

   public ComponentBuilder(UUID primordialUuid, int assemblageId) {
      this.primordialUuid = primordialUuid;
      this.assemblageId = assemblageId;
   }

   /**
    * Adds the semantic.
    *
    * @param semanticBuilder the semantic builder
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

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      if (!Get.identifierService().hasUuid(getUuids())) {
         return Get.identifierService().assignNid(getUuids());
      }
      else {
         return Get.identifierService().getNidForUuids(getUuids()); 
      }
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
      if (this.primordialUuid == null) {
         this.primordialUuid = Get.newUuidWithAssignment();  // This is a slow operation - lazy load.
      }

      return this.primordialUuid;
   }

   @Override
   public IdentifiedComponentBuilder<T> setPrimordialUuid(UUID uuid) {
      if (isPrimordialUuidSet()) {
         throw new RuntimeException("Attempting to set primordial UUID to: " + uuid.toString() +
                 " which has already been set to: " + getPrimordialUuid().toString());
      }
      this.primordialUuid = uuid;
      return this;
   }

   @Override
   public IdentifiedComponentBuilder<T> setStatus(Status status) {
      this.status = status;
      return this;
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
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
   
    @Override
    public List<SemanticBuilder<?>> getSemanticBuilders() {
        return semanticBuilders;
    }
    
    @Override
    public boolean isPrimordialUuidSet() {
        return this.primordialUuid != null;
    }

    @Override
    public void setModule(ConceptSpecification moduleSpecification) {
        this.module = Optional.of(moduleSpecification.getNid());
    }

    @Override
    public Optional<Integer> getModule() {
        return module;
    }
    
    protected WriteCoordinate adjustForModule(WriteCoordinate wc) {
        if (getModule().isPresent()) {
            return new WriteCoordinateImpl(wc.getAuthorNid(), getModule().get(), wc.getPathNid(), wc.getTransaction().get());
        }
        else {
            return wc;
        }
    }
}

