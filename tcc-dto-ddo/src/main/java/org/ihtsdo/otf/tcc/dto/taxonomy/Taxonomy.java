/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.dto.taxonomy;


//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.dto.JaxbForDto;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.UuidDtoBuilder;

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
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.dto.Wrapper;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

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
      this.pathSpec    = new ConceptSpec(pathName, getUuid(moduleName));
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
      this.authorSpec  = new ConceptSpec(authorName, getUuid(moduleName));
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
         dtoBuilder.construct(concept).writeExternal(out);
      }
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
        JAXBElement<Wrapper> jaxbElement = new JAXBElement<Wrapper>(qName,
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

   private UUID getUuid(String name)
           throws NoSuchAlgorithmException, UnsupportedEncodingException {
      return UuidT5Generator.get(this.getClass().getName() + name);
   }
}
