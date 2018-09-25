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

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

//~--- JDK imports ------------------------------------------------------------
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//~--- non-JDK imports --------------------------------------------------------
import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.description.DescriptionBuilder;
import sh.isaac.api.component.concept.description.DescriptionBuilderService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.MutableDescriptionVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.constants.MetadataConceptConstant;
import sh.isaac.api.constants.MetadataConceptConstantGroup;
import sh.isaac.api.constants.MetadataDynamicConstant;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.MultipleDataWriterService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.util.DescriptionToToken;
import sh.isaac.api.util.SemanticTags;
import sh.isaac.api.util.StringUtils;
import sh.isaac.api.util.metainf.VersionFinder;

//~--- classes ----------------------------------------------------------------
/**
 * Class for programatically creating and exporting a taxonomy.
 *
 * @author kec
 */
@Contract
public class IsaacTaxonomy {

   /**
    * The concept builders.
    */
   private final TreeMap<String, ConceptBuilder> conceptBuilders = new TreeMap<>();

   /**
    * The semantic builders.
    */
   private final List<SemanticBuilder<?>> semanticBuilders = new ArrayList<>();

   /**
    * The concept builders in insertion order.
    */
   private final List<ConceptBuilder> conceptBuildersInInsertionOrder = new ArrayList<>();

   /**
    * The parent stack.
    */
   private final Stack<ConceptBuilder> parentStack = new Stack<>();

   /**
    * The current.
    */
   private ConceptBuilder current;

   /**
    * The module spec.
    */
   private final ConceptSpecification moduleSpec;

   /**
    * The path spec.
    */
   private final ConceptSpecification pathSpec;

   /**
    * The author spec.
    */
   private final ConceptSpecification authorSpec;

   /**
    * The semantic tag.
    */
   private final String semanticTag;
   
   private final String auxiliaryMetadataVersion;
   private final UUID namespace;
   private final Logger LOG = LogManager.getLogger();

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new isaac taxonomy.
    *
    * @param path the path
    * @param author the author
    * @param module the module
    * @param semanticTag the semantic tag
    * @param auxiliaryMetadataVersion
    * @param namespaceForUUIDGeneration
    */
   public IsaacTaxonomy(ConceptSpecification path,
           ConceptSpecification author,
           ConceptSpecification module,
           String semanticTag, 
           String auxiliaryMetadataVersion, 
           UUID namespaceForUUIDGeneration) {
      this.pathSpec = path;
      this.authorSpec = author;
      this.moduleSpec = module;
      this.semanticTag = semanticTag;
      this.auxiliaryMetadataVersion = auxiliaryMetadataVersion;
      this.namespace = namespaceForUUIDGeneration;
   }

   //~--- methods -------------------------------------------------------------
   
   /**
    * Creates the concept.
    *
    * @param cc the cc
    * @return the concept builder
    * @throws Exception the exception
    */
   public ConceptBuilder createConcept(MetadataConceptConstant cc) throws Exception {
       return createConcept(cc, false);
   }
   /**
    * Creates the concept.
    *
    * @param cc the cc
    * @param isIdentifier
    * @return the concept builder
    * @throws Exception the exception
    */
   public final ConceptBuilder createConcept(MetadataConceptConstant cc, boolean isIdentifier)
           throws Exception {
      try {
         final ConceptBuilder cb = createConcept(cc.getFullyQualifiedName(),
                 (cc.getParent() != null) ? cc.getParent()
                 .getNid()
                 : null,
                 cc.getRegularName().orElse(null));

         cb.setPrimordialUuid(cc.getPrimordialUuid());

         cc.getDefinitions().forEach((definition) -> {
            addDescription(definition, cb, TermAux.DEFINITION_DESCRIPTION_TYPE, false);
         });

         cc.getSynonyms().forEach((synonym) -> {
            addDescription(synonym, cb, TermAux.REGULAR_NAME_DESCRIPTION_TYPE, false);
         });

         if (cc instanceof MetadataConceptConstantGroup) {
            pushParent(current());

            for (final MetadataConceptConstant nested : ((MetadataConceptConstantGroup) cc).getChildren()) {
               createConcept(nested);
            }

            popParent();
         }

         if (cc instanceof MetadataDynamicConstant) {
            // See {@link DynamicSemanticUsageDescription} class for more details on this format.
            final MetadataDynamicConstant dsc = (MetadataDynamicConstant) cc;
            final DescriptionBuilder<? extends SemanticChronology, ? extends MutableDescriptionVersion> db
                    = addDescription(dsc.getAssemblageDescription(),
                            cb,
                            TermAux.DEFINITION_DESCRIPTION_TYPE,
                            false);

            // Annotate the description as the 'special' type that means this concept is suitable for use as an assemblage concept
            SemanticBuilder<? extends SemanticChronology> sb = Get.semanticBuilderService()
                    .getDynamicBuilder(db,
                            DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION
                                    .getNid());

            db.addSemantic(sb);

            if (dsc.getDynamicColumns() != null) {
               for (final DynamicColumnInfo col : dsc.getDynamicColumns()) {
                  final DynamicData[] colData = LookupService.getService(DynamicUtility.class)
                          .configureDynamicDefinitionDataForColumn(col);

                  sb = Get.semanticBuilderService()
                          .getDynamicBuilder(cb,
                                  DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION
                                          .getNid(),
                                  colData);
                  cb.addSemantic(sb);
               }
            }

            final DynamicData[] data = LookupService.getService(DynamicUtility.class)
                    .configureDynamicRestrictionData(
                            dsc.getReferencedComponentTypeRestriction(),
                            dsc.getReferencedComponentSubTypeRestriction());

            if (data != null) {
               sb = Get.semanticBuilderService()
                       .getDynamicBuilder(cb,
                               DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION
                                       .getNid(),
                               data);
               cb.addSemantic(sb);
            }

            final DynamicArray<DynamicData> indexConfig
                    = LookupService.getService(DynamicUtility.class)
                            .configureColumnIndexInfo(dsc.getDynamicColumns());

            if (indexConfig != null) {
               sb = Get.semanticBuilderService()
                       .getDynamicBuilder(cb,
                               DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                                       .getNid(),
                               new DynamicData[]{indexConfig});
               cb.addSemantic(sb);
            }
         }
         
         cb.setT5UuidNested(namespace);
         if (isIdentifier) {
             addIdentifierAssemblageMembership(cb);
         }

         return cb;
      } catch (final Exception e) {
         throw new Exception("Problem with '" + cc.getFullyQualifiedName() + "'", e);
      }
   }

   /**
    * Export.
    *
    * @param jsonPath the json path
    * @param ibdfPath the ibdf path
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void export(Optional<Path> jsonPath, Optional<Path> ibdfPath)
           throws IOException {
      final long exportTime = System.currentTimeMillis();
      final int stampSequence = Get.stampService()
              .getStampSequence(Status.ACTIVE,
                      exportTime,
                      this.authorSpec.getNid(),
                      this.moduleSpec.getNid(),
                      this.pathSpec.getNid());
      final CommitService commitService = Get.commitService();
      final AssemblageService assemblageService = Get.assemblageService();
      final ConceptService conceptService = Get.conceptService();

      commitService.setComment(stampSequence, "Generated by maven from java sources");

      this.conceptBuildersInInsertionOrder.forEach((builder) -> {
         buildAndWriteToService(builder, stampSequence, conceptService, assemblageService);
      });

      this.semanticBuilders.forEach((builder) -> {
         buildAndWriteToService(builder, stampSequence, conceptService, assemblageService);
      });

      final int stampAliasForPromotion = Get.stampService()
              .getStampSequence(Status.ACTIVE,
                      exportTime + (1000 * 60),
                      this.authorSpec.getNid(),
                      this.moduleSpec.getNid(),
                      this.pathSpec.getNid());

      commitService.addAlias(stampSequence, stampAliasForPromotion, "promoted by maven");

      try (DataWriterService writer = new MultipleDataWriterService(jsonPath, ibdfPath)) {
         Get.isaacExternalizableStream()
                 .forEach((ochreExternalizable) -> writer.put(ochreExternalizable));
      }
   }

   /**
    * Export java binding.
    *
    * @param out the out
    * @param packageName the package name
    * @param className the class name
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void exportJavaBinding(Writer out, String packageName, String className)
           throws IOException {
      out.append("package " + packageName + ";\n");
      out.append("\n\nimport sh.isaac.api.component.concept.ConceptSpecification;\n");
      out.append("import sh.isaac.api.ConceptProxy;\n");
      out.append("import java.util.UUID;\n");
      
      out.append("\n//Generated " + new Date().toString() + "\n");
      out.append("//Pom Version " + VersionFinder.findProjectVersion(true) + "\n");
      

      out.append("\n\npublic class " + className + " {\n");

      out.append("\n\tpublic static final String AUXILIARY_METADATA_VERSION = \"" + auxiliaryMetadataVersion + "\";\n");
      
      ArrayList<String> constantList = new ArrayList<>();
      
      ArrayList<ConceptBuilder> sortedBuilders = new ArrayList<>(this.conceptBuildersInInsertionOrder);
      sortedBuilders.sort((o1, o2) -> {
          return o1.getFullySpecifiedDescriptionBuilder().getDescriptionText()
                  .compareTo(o2.getFullySpecifiedDescriptionBuilder().getDescriptionText()); 
      });

      for (final ConceptBuilder concept : sortedBuilders) {
         final String fqn = concept.getFullyQualifiedName();
         String constantName = DescriptionToToken.get(fqn.toUpperCase());
         out.append("\n\n   /** Java binding for the concept described as <strong><em>" + fqn
                 + "</em></strong>;\n    * identified by UUID: {@code \n    * "
                 + "<a href=\"http://localhost:8080/terminology/rest/concept/" + concept.getPrimordialUuid()
                 + "\">\n    * " + concept.getPrimordialUuid() + "</a>}.*/");
         constantList.add(constantName);
         out.append("\n   public static final ConceptSpecification " + constantName + " =");
         out.append("\n             new ConceptProxy(\"" + fqn + "\"");

         for (final UUID uuid : concept.getUuidList()) {
            out.append(", java.util.UUID.fromString(\"" + uuid.toString() + "\")");
         }

         out.append(");");
      }

      out.append("\n\n   public static final ConceptSpecification[] META_DATA_CONCEPTS = {\n");
      
      Collections.sort(constantList);
      for (int i = 0; i < constantList.size(); i++) {
          String constant = constantList.get(i);
          if (i != constantList.size() -1) {
              out.append("          ").append(constant).append(",\n");
          } else {
              out.append("          ").append(constant).append("\n");
          }
      }
      
      
      out.append("     };");
      
      
      out.append("\n}\n");
      out.close();
   }

   /**
    * Export yaml binding.
    *
    * @param out the out
    * @param packageName the package name
    * @param className the class name
     * @param additionalConstants
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void exportYamlBinding(Writer out, String packageName, String className, Map<String, MetadataConceptConstant> additionalConstants)
           throws IOException {
      out.append("#YAML Bindings for " + packageName + "." + className + "\n");
      out.append("#Generated " + new Date().toString() + "\n");
      out.append("#Pom Version " + VersionFinder.findProjectVersion(true) + "\n");
      out.append("\nAUXILIARY_METADATA_VERSION: " + auxiliaryMetadataVersion + "\n");
      
      HashSet<String> genConstants = new HashSet<>();

      for (final ConceptBuilder concept : this.conceptBuildersInInsertionOrder) {
          if (concept.getModule().isPresent() && concept.getModule().get().equals(TermAux.KOMET_MODULE)) {
              continue;
          }
         String conceptName = concept.getRegularName().orElse(SemanticTags.stripSemanticTagIfPresent(concept.getFullyQualifiedName()));
           
         if (conceptName.contains("↳"))  //This oddball causes problems all over
         {
             conceptName = SemanticTags.stripSemanticTagIfPresent(concept.getFullyQualifiedName());
         }
         String constantName = conceptName.toUpperCase();
         
         if (conceptName.indexOf("(") > 0 || conceptName.indexOf(")") > 0) {
             throw new RuntimeException("The metadata concept '" + conceptName + "' contains parens, which is illegal.");
         }

         constantName = DescriptionToToken.get(constantName);
         if (!genConstants.add(constantName)) {
            throw new RuntimeException("Duplicate definition of regular name for constant " + constantName + " " + concept.getFullyQualifiedName());
         }
         out.append("\n" + constantName + ":\n");
         out.append("    fqn: " + concept.getFullyQualifiedName() + "\n");
         out.append("    regular: " + conceptName + "\n");
         out.append("    uuids:\n");

         for (final UUID uuid : concept.getUuidList()) {
            out.append("        - " + uuid.toString() + "\n");
         }
      }
      
      if (additionalConstants != null)
      {
          for (Entry<String, MetadataConceptConstant> mcc : additionalConstants.entrySet())
          {
              String preferredName = mcc.getValue().getRegularName().orElse(SemanticTags.stripSemanticTagIfPresent(mcc.getValue().getFullyQualifiedName()));
              String constantName = mcc.getKey();

              if (preferredName.indexOf("(") > 0 || preferredName.indexOf(")") > 0) {
                  throw new RuntimeException("The metadata concept '" + preferredName + "' contains parens, which is illegal.");
              }
              constantName = constantName.replace(" ", "_");
              constantName = constantName.replace("-", "_");
              constantName = constantName.replace("+", "_PLUS");
              constantName = constantName.replace("/", "_AND");

              out.append("\n" + constantName + ":\n");
              out.append("    fqn: " + preferredName + "\n");
              out.append("    uuids:\n");
              for (UUID uuid : mcc.getValue().getUuidList()) {
                  out.append("        - " + uuid.toString() + "\n");
              }
          }
      }

      out.close();
   }

   /**
    * Adds the path.
    *
    * @param pathAssemblageConcept the path assemblage concept
    * @param pathConcept the path concept
     * @param semanticUuid the UUID to assign to the path semantic...
    */
   protected final void addPath(ConceptBuilder pathAssemblageConcept, ConceptBuilder pathConcept, UUID semanticUuid) {
       
       SemanticBuilder<? extends SemanticChronology> pathMemberBuilder = Get.semanticBuilderService()
              .getMembershipSemanticBuilder(pathConcept.getNid(),
                      pathAssemblageConcept.getNid());
       pathMemberBuilder.setPrimordialUuid(semanticUuid);
       
      this.semanticBuilders.add(pathMemberBuilder);
   }
   
   /**
    * Creates the concept.
    *
    * @param specification the concept specification
    * @return the concept builder
    */
   protected final ConceptBuilder createConcept(ConceptSpecification specification) {
      return createConcept(specification, null);
   }

   /**
    * Creates the concept.
    *
    * @param specification the concept specification
    * @param extraParent
    * @return the concept builder
    */
   protected final ConceptBuilder createConcept(ConceptSpecification specification, Integer extraParent) {
      final ConceptBuilder builder = createConcept(specification.getFullyQualifiedName(), null, extraParent, null);

      if (specification.getPrimordialUuid().version() == 4) {
         throw new UnsupportedOperationException("ERROR: must not use type 4 uuid for: " + specification.getFullyQualifiedName());
      }

      builder.setPrimordialUuid(specification.getUuidList().get(0));
      if (specification.getUuidList().size() > 1) {
         builder.addUuids(specification.getUuidList().subList(1, specification.getUuidList().size()).toArray(new UUID[0]));
      }

      if (specification instanceof ConceptProxy) {
         Optional<String> preferredDescription = ((ConceptProxy) specification).getRegularNameNoLookup();
         if (preferredDescription.isPresent()) {
            builder.getPreferredDescriptionBuilder().setDescriptionText(preferredDescription.get());
         }
      } else {
         Optional<String> preferredDescription = specification.getRegularName();
         if (preferredDescription.isPresent()) {
            builder.getPreferredDescriptionBuilder().setDescriptionText(preferredDescription.get());
         }
      }

      return builder;
   }
   
   private static <T extends CommittableComponent> IdentifiedComponentBuilder<T> addIdentifierAssemblageMembership(IdentifiedComponentBuilder<T> builder) {
      // add static member semantic
      return builder.addSemantic(Get.semanticBuilderService().getMembershipSemanticBuilder(builder, TermAux.IDENTIFIER_SOURCE.getNid()));
   }

   /**
    * Creates the concept.
    *
    * @param name the name
    * @return the concept builder
    */
   protected final ConceptBuilder createConcept(String name) {
      return createConcept(name, (Integer)null, (String)null);
   }

   /**
    * Creates the concept.
    *
    * @param name the name
    * @param nonPreferredSynonym the non preferred synonym
    * @return the concept builder
    */
   protected final ConceptBuilder createConcept(String name, String nonPreferredSynonym) {
      return createConcept(name, (Integer)null, nonPreferredSynonym);
   }
   
   protected final ConceptBuilder createConcept(String name, String nonPreferredSynonym, String definition) {
       ConceptBuilder cb = createConcept(name, (Integer)null, nonPreferredSynonym);

       if (StringUtils.isNotBlank(definition)) {
           addDescription(definition, cb, TermAux.DEFINITION_DESCRIPTION_TYPE, false);
       }
       return cb;
   }
   
   /**
    * If parent is provided, it ignores the parent stack, and uses the provided parent instead. If parent is not
    * provided, it uses the parentStack (if populated), otherwise, it creates the concept without setting a parent.
    *
    * @param name the name
    * @param parentId the parent id - if provided - this is the parent, if not provided, the primary parent comes 
    *                 from the parentStack
    * @param nonPreferredSynonym the non preferred synonym
    * @return the concept builder
    */
   protected final ConceptBuilder createConcept(String name, Integer parentId, String nonPreferredSynonym) {
      return createConcept(name, parentId, null, nonPreferredSynonym);
   }

   /**
    * If parent is provided, it ignores the parent stack, and uses the provided parent instead. If parent is not
    * provided, it uses the parentStack (if populated), otherwise, it creates the concept without setting a parent.
    *
    * @param name the name
    * @param parentId the parent id - if provided - this is the parent, if not provided, the primary parent comes 
    *                 from the parentStack
    * @param extraParent - optional additional parent (a second parent to the one in parentId/the stack).  Only used
    *                      when parentId is provided or the parentStack is not null.
    * @param nonPreferredSynonym the non preferred synonym
    * @return the concept builder
    */
   protected final ConceptBuilder createConcept(String name, Integer parentId, Integer extraParent, String nonPreferredSynonym) {
      checkConceptDescriptionText(name);

      if (this.parentStack.isEmpty() && (parentId == null)) {
         this.current = Get.conceptBuilderService()
                 .getDefaultConceptBuilder(name, this.semanticTag, null, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
      } else {
         final LogicalExpressionBuilderService expressionBuilderService
                 = LookupService.getService(LogicalExpressionBuilderService.class);
         final LogicalExpressionBuilder defBuilder = expressionBuilderService.getLogicalExpressionBuilder();

         if (extraParent != null) {
            NecessarySet(And(
                  ConceptAssertion((parentId == null) ? this.parentStack.lastElement().getNid() : parentId, defBuilder), 
                  ConceptAssertion(extraParent, defBuilder)));
         }
         else {
            NecessarySet(And(ConceptAssertion((parentId == null) ? this.parentStack.lastElement().getNid() : parentId, defBuilder)));
         }

         final LogicalExpression logicalExpression = defBuilder.build();

         this.current = Get.conceptBuilderService()
                 .getDefaultConceptBuilder(name, this.semanticTag, logicalExpression, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
      }

      if (org.apache.commons.lang3.StringUtils.isNotBlank(nonPreferredSynonym)) {
         this.current.addDescription(nonPreferredSynonym, TermAux.REGULAR_NAME_DESCRIPTION_TYPE);
      }

      this.conceptBuilders.put(name, this.current);
      this.conceptBuildersInInsertionOrder.add(this.current);
      return this.current;
   }

   /**
    * Current.
    *
    * @return the concept builder
    */
   protected final ConceptBuilder current() {
      return this.current;
   }

   /**
    * Export.
    *
    * @param dataOutputStream the data output stream
    */
   protected void export(DataOutputStream dataOutputStream) {
      throw new UnsupportedOperationException(
              "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Iterator over all of the concept builders, and 'fix' any that were entered without having their primordial UUID
    * set to a consistent value. The builder assigned a Type4 (random) UUID the first time that getPrimordialUuid() is
    * called - must override that UUID with one that we can consistently create upon each execution that builds the
    * MetaData constants.
    */
   protected final void generateStableUUIDs() {
      this.conceptBuilders.values().forEach((cb) -> {
         cb.setT5UuidNested(namespace);
      });
      this.semanticBuilders.forEach((sb) -> {
         sb.setT5UuidNested(namespace);
      });
   }

   /**
    * Pop parent.
    */
   protected final void popParent() {
      this.parentStack.pop();
   }

   /**
    * Push parent.
    *
    * @param parent the parent
    */
   protected final void pushParent(ConceptBuilder parent) {
      parent.setT5UuidNested(namespace);  // no generated UUIDs from this point on....
      this.parentStack.push(parent);
   }

   /**
    * type should be either {@link TermAux#DEFINITION_DESCRIPTION_TYPE} or {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE} This
    * currently only creates english language descriptions.
    *
    * @param description the description
    * @param cb the cb
    * @param descriptionType the description type
    * @param preferred the preferred
    * @return the description builder
    */
   private DescriptionBuilder<? extends SemanticChronology, ? extends MutableDescriptionVersion> addDescription(String description,
           ConceptBuilder cb,
           ConceptSpecification descriptionType,
           boolean preferred) {
      final DescriptionBuilder<? extends SemanticChronology, ? extends MutableDescriptionVersion> db
              = LookupService.getService(DescriptionBuilderService.class)
                      .getDescriptionBuilder(description,
                              cb,
                              descriptionType,
                              TermAux.ENGLISH_LANGUAGE);

      if (preferred) {
         db.addPreferredInDialectAssemblage(TermAux.US_DIALECT_ASSEMBLAGE);
      } else {
         db.addAcceptableInDialectAssemblage(TermAux.US_DIALECT_ASSEMBLAGE);
      }

      cb.addDescription(db);
      return db;
   }

   /**
    * Builds the and write.
    *
    * @param builder the builder
    * @param stampCoordinate the stamp coordinate
    * @param conceptService the concept service
    * @param assemblageService the assemblage service
    * @throws IllegalStateException the illegal state exception
    */
   private void buildAndWriteToService(IdentifiedComponentBuilder<? extends CommittableComponent> builder,
           int stampCoordinate,
           ConceptService conceptService,
           AssemblageService assemblageService)
           throws IllegalStateException {
      final List<Chronology> builtObjects = new ArrayList<>();

      builder.build(stampCoordinate, builtObjects);
      builtObjects.forEach((builtObject) -> {
         if (builtObject instanceof ConceptChronology) {
            conceptService.writeConcept(
                    (ConceptChronology) builtObject);
            ConceptChronology restored = conceptService.getConceptChronology(((ConceptChronology) builtObject).getNid());
            if (restored.getAssemblageNid() >= 0) {
               LOG.error("Bad restore of: " + restored);
            }
         } else if (builtObject instanceof SemanticChronology) {
            assemblageService.writeSemanticChronology((SemanticChronology) builtObject);
         } else {
            throw new UnsupportedOperationException("Can't handle: " + builtObject);
         }
      });
   }

   /**
    * Check concept description text.
    *
    * @param name the name
    */
   private void checkConceptDescriptionText(String name) {
      if (this.conceptBuilders.containsKey(name)) {
         throw new RuntimeException("Concept is already added: " + name);
      }
   }
}
