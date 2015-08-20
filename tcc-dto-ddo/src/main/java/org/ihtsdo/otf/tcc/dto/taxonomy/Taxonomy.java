/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.dto.taxonomy;


import gov.vha.isaac.ochre.api.MetadataConceptConstant;
import gov.vha.isaac.ochre.api.MetadataConceptConstantGroup;
import gov.vha.isaac.ochre.api.MetadataDynamicSememeConstant;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.model.constants.IsaacMetadataConstants;
import gov.vha.isaac.ochre.observable.model.ObservableFields;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import java.beans.PropertyVetoException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.dto.JaxbForDto;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.UuidDtoBuilder;
import org.ihtsdo.otf.tcc.dto.Wrapper;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicArray;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicBoolean;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicInteger;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicString;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicUUID;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;

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
   private final HashMap<UUID, List<TtkRefexDynamicMemberChronicle>> dynamicSememes = new HashMap<>();  //dynamic sememes are not supported by blueprints.  
   private final long time = System.currentTimeMillis();

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
   
   protected ConceptCB createConcept(MetadataDynamicSememeConstant cc) throws Exception {
       ConceptCB cab = createConcept((MetadataConceptConstant) cc);
       
       // See {@link DynamicSememeUsageDescriptionBI} class for more details on this format.
       
       DescriptionCAB dcab = addDescription(cc.getSememeAssemblageDescription(), cab, Snomed.DEFINITION_DESCRIPTION_TYPE.getPrimodialUuid());
       //Annotate the description as the 'special' type that means this concept is suitable for use as an assemblage concept
       addDynamicAnnotation(dcab.getComponentUuid(), IsaacMetadataConstants.DYNAMIC_SEMEME_ASSEMBLAGES.getUUID(), new TtkRefexDynamicData[0]);
       
       
        if (cc.getDynamicSememeColumns() != null) {
            for (DynamicSememeColumnInfo col : cc.getDynamicSememeColumns()) {
                TtkRefexDynamicData[] data = new TtkRefexDynamicData[7];
                data[0] = new TtkRefexDynamicInteger(col.getColumnOrder());
                data[1] = new TtkRefexDynamicUUID(col.getColumnDescriptionConcept());
                data[2] = new TtkRefexDynamicString(col.getColumnDataType().name());
                data[3] = TtkRefexDynamicData.convertPolymorphicDataColumn(col.getDefaultColumnValue(), col.getColumnDataType());
                data[4] = new TtkRefexDynamicBoolean(col.isColumnRequired());
                
                if (col.getValidator() != null) {
                    ArrayList<TtkRefexDynamicString> validators = new ArrayList<>();
                    for (int i = 0; i < col.getValidator().length; i++)
                    {
                        validators.add(new TtkRefexDynamicString(col.getValidator()[i].name()));
                    }
                    data[5] = new TtkRefexDynamicArray<TtkRefexDynamicString>(validators.toArray(new TtkRefexDynamicString[validators.size()]));
                }
                else {
                    data[5] = null;
                }
                
                if (col.getValidatorData() != null) {
                    ArrayList<TtkRefexDynamicData> validators = new ArrayList<>();
                    for (int i = 0; i < col.getValidatorData().length; i++)
                    {
                        validators.add(TtkRefexDynamicData.convertPolymorphicDataColumn(col.getValidatorData()[i], col.getValidatorData()[i].getDynamicSememeDataType()));
                    }
                    data[6] = new TtkRefexDynamicArray<TtkRefexDynamicData>(validators.toArray(new TtkRefexDynamicData[validators.size()]));
                }
                else {
                    data[6] = null;
                }
                
                addDynamicAnnotation(cab.getComponentUuid(), IsaacMetadataConstants.DYNAMIC_SEMEME_EXTENSION_DEFINITION.getUUID(), data);
            }
        }
        
        if (cc.getReferencedComponentTypeRestriction() != null && ObjectChronologyType.UNKNOWN_NID != cc.getReferencedComponentTypeRestriction()) {
            int size = 1;
            if (cc.getReferencedComponentSubTypeRestriction() != null &&  SememeType.UNKNOWN != cc.getReferencedComponentSubTypeRestriction()) {
                size = 2;
            }

            TtkRefexDynamicData[] data = new TtkRefexDynamicData[size];
            data[0] = new TtkRefexDynamicString(cc.getReferencedComponentTypeRestriction().name());
            if (size == 2) {
                data[1] = new TtkRefexDynamicString(cc.getReferencedComponentTypeRestriction().name());
            }
            
            addDynamicAnnotation(cab.getComponentUuid(), IsaacMetadataConstants.DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getUUID(), data);
        }
        
        if (cc.getRequiredIndexes() != null) {
            configureDynamicRefexIndexes(cab.getComponentUuid(), cc.getRequiredIndexes());
        }
       return cab;
   }
   
   /**
    * type should be either Snomed.DEFINITION_DESCRIPTION_TYPE.getPrimodialUuid() or Snomed.SYNONYM_DESCRIPTION_TYPE.getPrimodialUuid()
    */
   private DescriptionCAB addDescription(String description, ConceptCB concept, UUID type) throws IOException, InvalidCAB, ContradictionException
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
       return dCab;
   }
   
   protected ConceptCB createConcept(MetadataConceptConstantGroup ccg) throws Exception {
       ConceptCB temp = createConcept((MetadataConceptConstant)ccg);
       
       pushParent(current());
       for (MetadataConceptConstant cc : ccg.getChildren()) {
           if (cc instanceof MetadataConceptConstantGroup) {
               createConcept((MetadataConceptConstantGroup)cc);
           }
           else if (cc instanceof MetadataDynamicSememeConstant) {
               createConcept((MetadataDynamicSememeConstant)cc);
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
      UuidDtoBuilder dtoBuilder = new UuidDtoBuilder(time,
                                 authorSpec.getUuids()[0],
                                 pathSpec.getUuids()[0],
                                 moduleSpec.getUuids()[0]);

      for (ConceptCB concept : conceptBpsInInsertionOrder) {
            constructAndWrite(dtoBuilder, concept, out);
      }
   }

    private void constructAndWrite(UuidDtoBuilder dtoBuilder, ConceptCB concept, DataOutputStream out) throws ContradictionException, InvalidCAB, IOException {
        TtkConceptChronicle ttkConcept = dtoBuilder.construct(concept);
        addDynamicSememes(ttkConcept);
        ttkConcept.writeExternal(out);
    }
    
    /**
     * Since the CAB support was ripped out for dynamic sememes, we just write them directly into the TTKConcept before the TTKConcept is serialized.
     */
    private void addDynamicSememes(TtkConceptChronicle ttkConcept) {
        List<TtkRefexDynamicMemberChronicle> ds = dynamicSememes.get(ttkConcept.getPrimordialUuid());
        if (ds != null) {
            for (TtkRefexDynamicMemberChronicle s : ds) {
                ttkConcept.getRefsetMembersDynamic().add(s);
                addNestedDynamicSememes(s);  //In case the annotation has an annotation
            }
        }
        for (TtkDescriptionChronicle d : ttkConcept.getDescriptions()) {
        	addNestedDynamicSememes(d);
        }
        for (TtkRelationshipChronicle r : ttkConcept.getRelationships()) {
        	addNestedDynamicSememes(r);
        }
    }
    
    private void addNestedDynamicSememes(TtkComponentChronicle<?, ?> component)
    {
    	List<TtkRefexDynamicMemberChronicle> ds = dynamicSememes.get(component.getPrimordialUuid());
        if (ds != null) {
            for (TtkRefexDynamicMemberChronicle s : ds) {
                component.getAnnotationsDynamic().add(s);
                addNestedDynamicSememes(s);  //In case the annoation has an annotation
            }
        }
    }
    

    public void exportJaxb(DataOutputStream out) throws Exception {
        UuidDtoBuilder dtoBuilder = new UuidDtoBuilder(time,
                authorSpec.getUuids()[0],
                pathSpec.getUuids()[0],
                moduleSpec.getUuids()[0]);

        Marshaller marshaller = JaxbForDto.get().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "not-generated-yet.xsd");


        ArrayList<TtkConceptChronicle> taxonomyList = new ArrayList<>();
        for (ConceptCB concept : conceptBpsInInsertionOrder) {
            TtkConceptChronicle ttkConcept = dtoBuilder.construct(concept);
            addDynamicSememes(ttkConcept);
            taxonomyList.add(ttkConcept);
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
         
         if (preferredName.indexOf("(") > 0 || preferredName.indexOf(")") > 0) {
            throw new RuntimeException("The metadata concept '" + preferredName + "' contains parens, which is illegal.");
         }

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
     
    protected TtkRefexDynamicMemberChronicle addDynamicAnnotation(UUID component, UUID assemblageID, TtkRefexDynamicData[] data) 
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //TODO this should have a validator for data columns aligning with the refex description
        TtkRefexDynamicMemberChronicle dynamicSememe = new TtkRefexDynamicMemberChronicle();
        dynamicSememe.setComponentUuid(component);
        dynamicSememe.setRefexAssemblageUuid(assemblageID);
        dynamicSememe.setData(data);
        setUUIDForDynamicSememe(dynamicSememe, data, null);
        setRevisionAttributes(dynamicSememe, null);

        List<TtkRefexDynamicMemberChronicle> ds = dynamicSememes.get(component);
        if (ds == null) {
            ds = new ArrayList<>();
        }
        ds.add(dynamicSememe);
        return dynamicSememe;
    }
    
    /**
     * Set up all the boilerplate stuff.
     * 
     * @param object - The object to do the setting to
     * @param statusUuid - Uuid or null (for current)
     */
    public void setRevisionAttributes(TtkRevision object, Status status) {
        object.setAuthorUuid(authorSpec.getPrimodialUuid());
        object.setModuleUuid(moduleSpec.getPrimodialUuid());
        object.setPathUuid(pathSpec.getPrimodialUuid());
        object.setStatus(status == null ? Status.ACTIVE : status);
        object.setTime(time);
    }
     
    /**
     * @param namespace - optional - uses {@link DynamicSememe#DYNAMIC_SEMEME_NAMESPACE} if not specified
     * @return - the generated string used for refex creation 
     */
    public static String setUUIDForDynamicSememe(TtkRefexDynamicMemberChronicle dynamicSememe, TtkRefexDynamicData[] data, UUID namespace) throws NoSuchAlgorithmException, 
        UnsupportedEncodingException {
        //TODO dan - need to look and see how I am generating UUIDs for dynamic refexes in the Builder...
        StringBuilder sb = new StringBuilder();
        sb.append(dynamicSememe.getRefexAssemblageUuid().toString()); 
        sb.append(dynamicSememe.getComponentUuid().toString());
        if (data != null) {
            for (TtkRefexDynamicData d : data) {
                if (d == null) {
                    sb.append("null");
                }
                else {
                    sb.append(d.getRefexDataType().getDisplayName());
                    sb.append(new String(d.getData()));
                }
            }
        }
        dynamicSememe.setPrimordialComponentUuid(UuidT5Generator.get((namespace == null ? RefexCAB.refexSpecNamespace : namespace), sb.toString()));
        return sb.toString();
    }
    
    protected void configureDynamicRefexIndexes(UUID sememeToIndex, Integer[] columnConfiguration) 
            throws NoSuchAlgorithmException, UnsupportedEncodingException, PropertyVetoException {
        TtkRefexDynamicData[] data = null;
        if (columnConfiguration != null && columnConfiguration.length > 0) {
            data = new TtkRefexDynamicData[1];
            TtkRefexDynamicInteger[] cols = new TtkRefexDynamicInteger[columnConfiguration.length];
            for (int i = 0; i < columnConfiguration.length; i++) {
                cols[i] = new TtkRefexDynamicInteger(columnConfiguration[i]);
            }

            data[0] = new TtkRefexDynamicArray<TtkRefexDynamicData>(cols);
        }
        
        addDynamicAnnotation(sememeToIndex, IsaacMetadataConstants.DYNAMIC_SEMEME_INDEX_CONFIGURATION.getUUID(), data);
    }
}
