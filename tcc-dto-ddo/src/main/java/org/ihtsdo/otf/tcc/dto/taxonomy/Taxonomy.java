/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.dto.taxonomy;


//~--- JDK imports ------------------------------------------------------------

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.dto.JaxbForDto;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.UuidDtoBuilder;
import org.ihtsdo.otf.tcc.dto.Wrapper;

import gov.vha.isaac.ochre.api.MetadataConceptConstant;
import gov.vha.isaac.ochre.api.MetadataConceptConstantGroup;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.observable.model.ObservableFields;
import gov.vha.isaac.ochre.util.UuidT5Generator;

/**
 *
 * @author kec
 */
public class Taxonomy {
   private final TreeMap<String, ConceptCB> conceptBps = new TreeMap<>();
   private final List<ConceptCB>            conceptBpsInInsertionOrder = new ArrayList<>();
   private final Stack<ConceptCB>           parentStack = new Stack<>();
   private ConceptCB                  current;
   private final ConceptSpec                isaTypeSpec;
   private final ConceptSpec                moduleSpec;
   private final ConceptSpec                pathSpec;
   private final ConceptSpec                authorSpec;
   private final String                     semanticTag;
   private final LanguageCode                lang;

   public Taxonomy(ConceptSpec path, ConceptSpec author, ConceptSpec module,
                   ConceptSpec isaType, String semanticTag, LanguageCode lang) {
      this.pathSpec    = path;
      this.authorSpec  = author;
      this.moduleSpec  = module;
      this.isaTypeSpec = isaType;
      this.semanticTag = semanticTag;
      this.lang        = lang;
   }

   public Taxonomy(ConceptSpec pathSpec, ConceptSpec authorSpec,
                   String moduleName, ConceptSpec isaType, String semanticTag,
                   LanguageCode lang)
           throws NoSuchAlgorithmException, UnsupportedEncodingException {
      this.pathSpec    = pathSpec;
      this.authorSpec  = authorSpec;
      this.moduleSpec  = new ConceptSpec(moduleName, getUuid(moduleName));
      this.isaTypeSpec = isaType;
      this.semanticTag = semanticTag;
      this.lang        = lang;
   }

   public Taxonomy(String pathName, ConceptSpec author, String moduleName,
                   ConceptSpec isaType, String semanticTag, LanguageCode lang)
           throws NoSuchAlgorithmException, UnsupportedEncodingException {
      this.pathSpec    = new ConceptSpec(pathName, getUuid(pathName));
      this.authorSpec  = author;
      this.moduleSpec  = new ConceptSpec(moduleName, getUuid(moduleName));
      this.isaTypeSpec = isaType;
      this.semanticTag = semanticTag;
      this.lang        = lang;
   }

   public Taxonomy(String pathName, String authorName, String moduleName,
                   ConceptSpec isaType, String semanticTag, LanguageCode lang)
           throws NoSuchAlgorithmException, UnsupportedEncodingException {
      this.pathSpec    = new ConceptSpec(pathName, getUuid(pathName));
      this.authorSpec  = new ConceptSpec(authorName, getUuid(authorName));
      this.moduleSpec  = new ConceptSpec(moduleName, getUuid(moduleName));
      this.isaTypeSpec = isaType;
      this.semanticTag = semanticTag;
      this.lang        = lang;
   }

   protected ConceptCB createConcept(String name) throws Exception {
      ConceptCB cb = new ConceptCB(name + " " + semanticTag, 
              name, lang, 
              isaTypeSpec.getUuids()[0],
              IdDirective.GENERATE_HASH,
              moduleSpec.getUuids()[0], 
              pathSpec.getUuids()[0],
              getParentArray());

      if (conceptBps.containsKey(name)) {
         throw new Exception("Concept is already added");
      }

      conceptBps.put(name, cb);
      conceptBpsInInsertionOrder.add(cb);
      current = cb;

      return cb;
   }
   
   protected ConceptCB createConcept(MetadataConceptConstant cc) throws Exception {
       ConceptCB cab = createConcept(cc.getFSN());
       cab.setPreferredName(cc.getPreferredSynonym());
       cab.setComponentUuidNoRecompute(cc.getUUID());
       
       for (String definition : cc.getDefinitions()) {
           addDescription(definition, cab, Snomed.DEFINITION_DESCRIPTION_TYPE.getPrimodialUuid());
       }
       
       for (String definition : cc.getSynonyms()) {
           addDescription(definition, cab, Snomed.SYNONYM_DESCRIPTION_TYPE.getPrimodialUuid());
       }
       
       return cab;
   }
   
   /**
    * type should be either Snomed.DEFINITION_DESCRIPTION_TYPE.getPrimodialUuid() or Snomed.SYNONYM_DESCRIPTION_TYPE.getPrimodialUuid()
    */
   private void addDescription(String description, ConceptCB concept, UUID type) throws IOException, InvalidCAB, ContradictionException
   {
       DescriptionCAB dCab = new DescriptionCAB(concept.getComponentUuid(), type, lang, description, true,
               IdDirective.GENERATE_HASH);
       dCab.getProperties().put(ComponentProperty.MODULE_ID, moduleSpec.getUuids()[0]);

       //Mark it as acceptable
       RefexCAB rCabAcceptable = new RefexCAB(RefexType.CID, dCab.getComponentUuid(),  Snomed.US_LANGUAGE_REFEX.getPrimodialUuid(),
               IdDirective.GENERATE_HASH, RefexDirective.EXCLUDE);
       rCabAcceptable.put(ComponentProperty.COMPONENT_EXTENSION_1_ID,  SnomedMetadataRf2.ACCEPTABLE_RF2.getPrimodialUuid());
       rCabAcceptable.getProperties().put(ComponentProperty.MODULE_ID, moduleSpec.getUuids()[0]);
       dCab.addAnnotationBlueprint(rCabAcceptable);
       
       concept.addDescriptionCAB(dCab);
   }
   
   protected ConceptCB createConcept(MetadataConceptConstantGroup ccg) throws Exception {
       ConceptCB temp = createConcept((MetadataConceptConstant)ccg);
       
       pushParent(current());
       for (MetadataConceptConstant cc : ccg.getChildren()) {
           if (cc instanceof MetadataConceptConstantGroup) {
               createConcept((MetadataConceptConstantGroup)cc);
           }
           else {
               createConcept(cc);
           }
       }
       popParent();
       return temp;
   }

   protected ConceptCB createModuleConcept(String name) throws Exception {
      ConceptCB cb = new ConceptCB(name + " " + semanticTag, 
              name, lang, 
              isaTypeSpec.getUuids()[0],
              IdDirective.GENERATE_HASH,
              moduleSpec.getUuids()[0], 
              pathSpec.getUuids()[0],
              
              getParentArray());

      if (conceptBps.containsKey(name)) {
         throw new Exception("Concept is already added");
      }

      conceptBps.put(name, cb);
      conceptBpsInInsertionOrder.add(cb);
      current = cb;

      return cb;
   }

   public void exportEConcept(DataOutputStream out) throws Exception {
      UuidDtoBuilder dtoBuilder = new UuidDtoBuilder(System.currentTimeMillis(),
                                 authorSpec.getUuids()[0],
                                 pathSpec.getUuids()[0],
                                 moduleSpec.getUuids()[0]);

      for (ConceptCB concept : conceptBpsInInsertionOrder) {
            constructAndWrite(dtoBuilder, concept, out);
      }
   }

    private void constructAndWrite(UuidDtoBuilder dtoBuilder, ConceptCB concept, DataOutputStream out) throws ContradictionException, InvalidCAB, IOException {
        TtkConceptChronicle ttkConcept = dtoBuilder.construct(concept);
        ttkConcept.writeExternal(out);
    }

    public void exportJaxb(DataOutputStream out) throws Exception {
        UuidDtoBuilder dtoBuilder = new UuidDtoBuilder(System.currentTimeMillis(),
                authorSpec.getUuids()[0],
                pathSpec.getUuids()[0],
                moduleSpec.getUuids()[0]);

        Marshaller marshaller = JaxbForDto.get().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "not-generated-yet.xsd");


        ArrayList<TtkConceptChronicle> taxonomyList = new ArrayList<>();
        for (ConceptCB concept : conceptBpsInInsertionOrder) {
            taxonomyList.add(dtoBuilder.construct(concept));
        }


        QName qName = new QName("taxonomy");
        Wrapper wrapper = new Wrapper(taxonomyList);
        JAXBElement<Wrapper> jaxbElement = new JAXBElement<>(qName,
                Wrapper.class, wrapper);
        marshaller.marshal(jaxbElement, out);

    }

    public void exportJavaBinding(Writer out, String packageName,
                                 String className)
           throws IOException {
      out.append("package " + packageName + ";\n");
      out.append("\n\nimport java.util.UUID;\n");
      out.append("import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;\n");
      out.append("\n\npublic class " + className + " {\n");

      for (ConceptCB concept : conceptBpsInInsertionOrder) {
         String preferredName = concept.getPreferredName();
         String constantName  = preferredName.toUpperCase();

         constantName = constantName.replace(" ", "_");
         constantName = constantName.replace("-", "_");
         constantName = constantName.replace("+", "_PLUS");
         constantName = constantName.replace("/", "_AND");
         out.append("\n\n   /** Java binding for the concept described as <strong><em>"
                 + preferredName
                 + "</em></strong>;\n    * identified by UUID: {@code \n    * "
                 + "<a href=\"http://localhost:8080/terminology/rest/concept/"
                 + concept.getComponentUuid().toString()
                 + "\">\n    * "
                 + concept.getComponentUuid().toString()
                 + "</a>}.*/");
         
         out.append("\n   public static ConceptSpec " + constantName + " =");
         out.append("\n             new ConceptSpec(\"" + preferredName
                    + "\",");
         out.append("\n                    UUID.fromString(\""
                    + concept.getComponentUuid().toString() + "\"));");
      }

      out.append("\n}\n");
   }

   protected ConceptCB current() {
      return current;
   }

   protected void popParent() {
      parentStack.pop();
   }

   protected void pushParent(ConceptCB parent) {
      parentStack.push(parent);
   }

   private UUID[] getParentArray() {
      if (parentStack.size() == 0) {
         return new UUID[0];
      }

      return new UUID[] { parentStack.peek().getComponentUuid() };
   }

   protected final UUID getUuid(String name)
           throws NoSuchAlgorithmException, UnsupportedEncodingException {
      return UuidT5Generator.get(this.getClass().getName() + name);
   }
     protected ConceptCB createConcept(ObservableFields observableFields) throws Exception {
      ConceptCB cb = new ConceptCB(observableFields.getDescription() + " " + semanticTag, 
              observableFields.getDescription(), lang, 
              isaTypeSpec.getUuids()[0],
              IdDirective.GENERATE_HASH,
              moduleSpec.getUuids()[0], 
              pathSpec.getUuids()[0],
              getParentArray());
      
      if (conceptBps.containsKey(observableFields.name())) {
         throw new Exception("Concept is already added");
      }

      conceptBps.put(observableFields.name(), cb);
      conceptBpsInInsertionOrder.add(cb);
      current = cb;
      cb.setComponentUuidNoRecompute(observableFields.getUuid());
      return cb;
   }  

}
