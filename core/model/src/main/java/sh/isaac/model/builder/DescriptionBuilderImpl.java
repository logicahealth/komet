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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
import sh.isaac.api.task.OptionalWaitTask;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.api.util.UuidT5Generator;

/**
 * The Class DescriptionBuilderImpl.
 *
 * @author kec
 * @param <T> the generic type
 * @param <V> the value type
 */
public class DescriptionBuilderImpl<T extends SemanticChronology, V extends DescriptionVersion>
        extends ComponentBuilder<T>
         implements DescriptionBuilder<T, V> {
   /** The preferred in dialect assemblages. */
    private final HashMap<ConceptSpecification, SemanticBuilder<?>> preferredInDialectAssemblages = new HashMap<>();
    
    private final HashMap<ConceptSpecification, SemanticBuilder<?>> acceptableInDialectAssemblages = new HashMap<>();

   /** The concept nid. */
   private int conceptNid = Integer.MAX_VALUE;

   /** The description text. */
   private String descriptionText;

   /** The description type. */
   private final ConceptSpecification descriptionType;

   /** The language for description. */
   private final ConceptSpecification languageForDescription;

   /** The concept builder. */
   private final ConceptBuilder conceptBuilder;


   /**
    * Instantiates a new description builder.
    *
    * @param descriptionText the description text
    * @param conceptBuilder the concept builder
    * @param descriptionType the description type
    * @param languageForDescription the language for description - also used as the assemblage
    */
   public DescriptionBuilderImpl(String descriptionText,
                                      ConceptBuilder conceptBuilder,
                                      ConceptSpecification descriptionType,
                                      ConceptSpecification languageForDescription) {
      super(languageForDescription.getNid());
      this.descriptionText        = descriptionText;
      this.descriptionType        = descriptionType;
      this.languageForDescription = languageForDescription;
      this.conceptBuilder         = conceptBuilder;
   }

   /**
    * Instantiates a new description builder.
    *
    * @param descriptionText the description text
    * @param conceptNid the concept nid
    * @param descriptionType the description type
    * @param languageForDescription the language for description - also used as the assemblage
    */
   public DescriptionBuilderImpl(String descriptionText,
                                      int conceptNid,
                                      ConceptSpecification descriptionType,
                                      ConceptSpecification languageForDescription) {
      super(languageForDescription.getNid());
      this.descriptionText        = descriptionText;
      this.conceptNid        = conceptNid;
      this.descriptionType        = descriptionType;
      this.languageForDescription = languageForDescription;
      this.conceptBuilder         = null;
   }


   @Override
   public DescriptionBuilder addAcceptableInDialectAssemblage(ConceptSpecification dialectAssemblage) {
      this.acceptableInDialectAssemblages.put(dialectAssemblage, null);
      return this;
   }

   @Override
   public DescriptionBuilder addPreferredInDialectAssemblage(ConceptSpecification dialectAssemblage) {
      this.preferredInDialectAssemblages.put(dialectAssemblage, null);
      return this;
   }

   private SemanticBuilder<? extends SemanticChronology> buildInternal(List<Chronology> builtObjects) throws IllegalStateException {
      if (this.conceptNid == Integer.MAX_VALUE) {
         this.conceptNid = Get.identifierService().getNidForUuids(this.conceptBuilder.getUuids());
      }

      final SemanticBuilderService semanticBuilder = LookupService.getService(SemanticBuilderService.class);
      final SemanticBuilder<? extends SemanticChronology> descBuilder =
         semanticBuilder.getDescriptionBuilder(TermAux.caseSignificanceToConceptNid(false),
                                                   this.languageForDescription.getNid(),
                                                   this.descriptionType.getNid(),
                                                   this.descriptionText,this.conceptNid);

      descBuilder.setPrimordialUuid(this.getPrimordialUuid());
      return descBuilder;
   }

   @Override
    public T build(Transaction transaction, int stampSequence, List<Chronology> builtObjects) throws IllegalStateException {
       WriteCoordinate main = adjustForModule(new WriteCoordinateImpl(transaction, stampSequence));
       
       SemanticBuilder<? extends SemanticChronology> descBuilder = buildInternal(builtObjects);
       final SemanticChronology newDescription = (SemanticChronology) descBuilder.build(
             status == null ? main : new WriteCoordinateImpl(new WriteCoordinateImpl(transaction, stampSequence), status), builtObjects);
       getSemanticBuilders().forEach((builder) -> builder.build(main, builtObjects));
       return (T) newDescription;
   }
   
   @Override
   public OptionalWaitTask<T> buildAndWrite(WriteCoordinate writeCoordinate, List<Chronology> builtObjects) throws IllegalStateException
   {
      final ArrayList<OptionalWaitTask<?>> nestedBuilders = new ArrayList<>();
      WriteCoordinate main = adjustForModule(writeCoordinate);

      SemanticBuilder<? extends SemanticChronology> descBuilder = buildInternal(builtObjects);
      
      final OptionalWaitTask<SemanticChronology> newDescription = (OptionalWaitTask<SemanticChronology>) descBuilder.buildAndWrite(
            status == null ? main : new WriteCoordinateImpl(main, status), builtObjects);
      getSemanticBuilders().forEach((builder) -> nestedBuilders.add(builder.buildAndWrite(main, builtObjects)));
      return new OptionalWaitTask<>(null, (T) newDescription.getNoWait(), nestedBuilders);
   }

   @Override
   public String getDescriptionText() {
      return descriptionText;
   }

   @Override
   public DescriptionBuilder setDescriptionText(String descriptionText) {
      this.descriptionText = descriptionText;
      return this;
   }
   
   @Override
   public DescriptionBuilder setT5Uuid(UUID namespace, BiConsumer<String, UUID> consumer) {
      if (isPrimordialUuidSet() && getPrimordialUuid().version() == 4) {
         throw new RuntimeException("Attempting to set Type 5 UUID where the UUID was previously set to random");
      }

      if (!isPrimordialUuidSet()) {
         int caseSigNid = Get.languageCoordinateService().caseSignificanceToConceptNid(false);

         setPrimordialUuid(UuidFactory.getUuidForDescriptionSemantic(namespace == null ? UuidT5Generator.PATH_ID_FROM_FS_DESC : namespace,
                 conceptBuilder == null ? Get.identifierService().getUuidPrimordialForNid(conceptNid) : conceptBuilder.getPrimordialUuid(), 
                 Get.identifierService().getUuidPrimordialForNid(caseSigNid),
                 descriptionType.getPrimordialUuid(), 
                 languageForDescription.getPrimordialUuid(), 
                 descriptionText,
                 consumer));
      }
      return this;
   }

   @Override
   public IdentifiedComponentBuilder<T> setT5UuidNested(UUID namespace) {
      setT5Uuid(namespace, null);
      for (SemanticBuilder<?> sb : getSemanticBuilders()) {
         sb.setT5UuidNested(namespace);
      }
      return this;
   }

   @Override
   public List<SemanticBuilder<?>> getSemanticBuilders() {
      ArrayList<SemanticBuilder<?>> temp = new ArrayList<>(super.getSemanticBuilders().size() + preferredInDialectAssemblages.size() 
          + acceptableInDialectAssemblages.size());

      temp.addAll(super.getSemanticBuilders());

      SemanticBuilderService semanticBuilderService = LookupService.getService(SemanticBuilderService.class);

      for (Entry<ConceptSpecification, SemanticBuilder<?>> p : preferredInDialectAssemblages.entrySet()) {
         if (p.getValue() == null) {
            p.setValue(semanticBuilderService.getComponentSemanticBuilder(TermAux.PREFERRED.getNid(), this,
               p.getKey().getNid()));
         }
         temp.add(p.getValue());
      }

      for (Entry<ConceptSpecification, SemanticBuilder<?>> a : acceptableInDialectAssemblages.entrySet()) {
         if (a.getValue() == null) {
            a.setValue(semanticBuilderService.getComponentSemanticBuilder(TermAux.ACCEPTABLE.getNid(), this,
               a.getKey().getNid()));
         }
         temp.add(a.getValue());
      }
      return temp;
   }

   @Override
   public String toString() {
      return "DescriptionBuilderImpl{" + "descriptionText=" + descriptionText + '}';
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VersionType getVersionType() {
      return VersionType.DESCRIPTION;
   }
}