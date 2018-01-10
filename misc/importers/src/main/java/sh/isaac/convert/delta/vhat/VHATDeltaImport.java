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
package sh.isaac.convert.delta.vhat;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import sh.isaac.MetaData;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.MutableDescriptionVersion;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableStringVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Associations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyAssociation;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.mapping.constants.IsaacMappingConstants;
import sh.isaac.misc.constants.VHATConstants;
import sh.isaac.misc.constants.terminology.data.ActionType;
import sh.isaac.misc.constants.terminology.data.ConceptType;
import sh.isaac.misc.constants.terminology.data.DesignationType;
import sh.isaac.misc.constants.terminology.data.PropertyType;
import sh.isaac.misc.constants.terminology.data.Terminology;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.Properties;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships.SubsetMembership;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties.Property;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Relationships.Relationship;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets.MapSet;
import sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry;
import sh.isaac.misc.constants.terminology.data.Terminology.Subsets.Subset;
import sh.isaac.misc.constants.terminology.data.Terminology.Types.Type;
import sh.isaac.misc.modules.vhat.VHATIsAHasParentSynchronizingChronologyChangeListener;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.coordinate.EditCoordinateImpl;
import sh.isaac.model.coordinate.ManifoldCoordinateImpl;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.model.semantic.version.DynamicImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.utility.Frills;

/**
 * Goal which converts VHAT data into the workbench jbin format
 * 
 */
public class VHATDeltaImport extends ConverterBaseMojo
{
   private IBDFCreationUtility importUtil_;
   private Map<String, UUID> extendedDescriptionTypeNameMap = new HashMap<>();
   
   private sh.isaac.converters.sharedUtils.propertyTypes.PropertyType associations = new BPT_Associations("VHAT");
   private sh.isaac.converters.sharedUtils.propertyTypes.PropertyType annotations = new BPT_Annotations("VHAT");
   private BPT_Refsets subsets = new BPT_Refsets("VHAT");
   private Map<Long, UUID> vuidToSubsetMap = new HashMap<>();
   private StampCoordinate readCoordinate;
   private LogicCoordinate logicReadCoordinate;
   private EditCoordinate editCoordinate;
   private LongSupplier vuidSupplier;
   private HashSet<String> conceptsToBeCreated = new HashSet<>();
   
   private static final Logger LOG = LogManager.getLogger();
   
   /***
    * @param xmlData The data to import
    * @param author The user to attribute the changes to
    * @param module The module to put the changes on
    * @param path The path to put the changes on
    * @param vuidSupplier (optional) a supplier that provides vuids, or null, if no automated vuid assignment is desired
    * @param debugOutputFolder (optional) a path to write json debug to, if provided.
    * @throws IOException
    */
   public VHATDeltaImport(String xmlData, UUID author, UUID module, UUID path, LongSupplier vuidSupplier, File debugOutputFolder) throws IOException
   {
      this.vuidSupplier = vuidSupplier;
      try
      {
         LOG.debug("Processing passed in XML data of length " + xmlData.length());
         try
         {
            schemaValidate(xmlData);
         }
         catch (SAXException | IOException e)
         {
            LOG.info("Submitted xml data failed schema validation", e);
            throw new IOException("The provided XML data failed Schema Validation.  Details: " + e.toString());
         }
         
         LOG.debug("Passed in VHAT XML data is schema Valid");
         Terminology terminology;
         
         try
         {
            JAXBContext jaxbContext = JAXBContext.newInstance(Terminology.class);
   
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xmlData));
            
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            terminology = (Terminology) jaxbUnmarshaller.unmarshal(xsr);
         }
         catch (JAXBException | XMLStreamException e)
         {
            LOG.error("Unexpected error parsing submitted VETs XML.", e);
            throw new IOException("Unexpected error parsing the xml.  Details: " + e.toString());
         }
         
         LOG.info("VHA XML Parsed");
         
         extendedDescriptionTypeNameMap.put(VHATConstants.VHAT_ABBREVIATION.getPrimaryName().toLowerCase(), VHATConstants.VHAT_ABBREVIATION.getPrimordialUuid());
         extendedDescriptionTypeNameMap.put(VHATConstants.VHAT_FULLY_SPECIFIED_NAME.getPrimaryName().toLowerCase(), VHATConstants.VHAT_FULLY_SPECIFIED_NAME.getPrimordialUuid());
         extendedDescriptionTypeNameMap.put(VHATConstants.VHAT_PREFERRED_NAME.getPrimaryName().toLowerCase(), VHATConstants.VHAT_PREFERRED_NAME.getPrimordialUuid());
         extendedDescriptionTypeNameMap.put(VHATConstants.VHAT_SYNONYM.getPrimaryName().toLowerCase(), VHATConstants.VHAT_SYNONYM.getPrimordialUuid());
         extendedDescriptionTypeNameMap.put(VHATConstants.VHAT_VISTA_NAME.getPrimaryName().toLowerCase(), VHATConstants.VHAT_VISTA_NAME.getPrimordialUuid());
         
         ConverterUUID.configureNamespace(TermAux.VHAT_MODULES.getPrimordialUuid());
         
         try
         {
            NidSet modulesToRead = new NidSet();
            modulesToRead.add(MetaData.MODULE____SOLOR.getNid());
            modulesToRead.add(MetaData.VHAT_MODULES____SOLOR.getNid());
            Frills.getAllChildrenOfConcept(MetaData.VHAT_MODULES____SOLOR.getNid(), true, false).forEach(i -> modulesToRead.add(i));
            
            this.readCoordinate = new StampCoordinateImpl(StampPrecedence.PATH,  
               new StampPositionImpl(Long.MAX_VALUE, TermAux.DEVELOPMENT_PATH.getNid()),
               modulesToRead, Status.ANY_STATUS_SET);
            this.editCoordinate = new EditCoordinateImpl(Get.identifierService().getNidForUuids(author), 
               Get.identifierService().getNidForUuids(module), Get.identifierService().getNidForUuids(path));
            
            this.logicReadCoordinate = LogicCoordinates.getStandardElProfile();
         }
         catch (Exception e)
         {
            throw new IOException("Unexpected error setting up", e);
         }
         
         headerCheck(terminology);
         vuidCheck(terminology);
         populateNewProperties(terminology);
         populateNewSubsets(terminology);
         requiredChecks(terminology);
         
         try
         {
            // Disable VHATIsAHasParentSynchronizingChronologyChangeListener listener
            // because the import generates all necessary components
            LookupService.getService(VHATIsAHasParentSynchronizingChronologyChangeListener.class).disable();

            importUtil_ = new IBDFCreationUtility(author, module, path, debugOutputFolder);
            LOG.info("Import Util configured");
            createNewProperties(terminology);
            createNewSubsets(terminology);
            
            LOG.info("Processing changes");
            loadConcepts(terminology.getCodeSystem().getVersion().getCodedConcepts());
            loadMapSets(terminology.getCodeSystem().getVersion().getMapSets());
            
            
            LOG.info("Committing Changes");
            CommitTask ct = Get.commitService().commit(this.editCoordinate, "VHAT Delta file");
            
            if (ct.get().isPresent())
            {
               LOG.info("Load complete!");
            }
            else
            {
               LOG.error("commit failed to process!");
               for (Alert a : ct.getAlerts())
               {
                  //TODO [DAN 1] fix alerts
                  //LOG.error(a.getAlertType().name() + ": " + a.getAlertText());
               }
               throw new RuntimeException("Unexpected internal error!");
            }
         }
         catch (RuntimeException e)
         {
            Get.commitService().cancel(this.editCoordinate);
            throw e;
         }
         catch (Exception e)
         {
            LOG.warn("Unexpected error setting up", e);
            throw new IOException("Unexpected error setting up", e);
         } finally {
            LookupService.getService(VHATIsAHasParentSynchronizingChronologyChangeListener.class).enable();
         }
      }
      catch (RuntimeException | IOException e)
      {
         LOG.info("Input XML not being processed because: ", e);
         throw e;
      }
      catch (Throwable e)
      {
         LOG.warn("Inpux XML processing failure",  e);
         throw e;
      }
      finally
      {
         ConverterUUID.clearCache();
      }
   }

   private void schemaValidate(String xmlData) throws SAXException, IOException
   {
      LOG.info("Doing schema validation");
      
      URL url = VHATDeltaImport.class.getResource("/TerminologyData.xsd.hidden");
      if (url == null)
      {
         throw new RuntimeException("Unable to locate the schema file!");
      }
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);     
      Schema schema = factory.newSchema(url);     
      Validator validator = schema.newValidator();     
      validator.validate(new StreamSource(new StringReader(xmlData)));
   }
   
   private void vuidCheck(Terminology terminology)
   {
      LOG.info("Checking for in use VUIDs");
      HashSet<Long> vuidsInXmlFile_ = new HashSet<>();
      
      if (terminology.getCodeSystem().getAction() == ActionType.ADD && terminology.getCodeSystem().getVUID() != null)
      {
         if (Frills.getNidForVUID(terminology.getCodeSystem().getVUID()).isPresent())
         {
            throw new RuntimeException("The VUID specified for the new code system '" + terminology.getCodeSystem().getName() + "' : '" 
                  + terminology.getCodeSystem().getVUID() + "' is already in use");
         }
         else if (!vuidsInXmlFile_.add(terminology.getCodeSystem().getVUID()))
         {
            throw new RuntimeException("The VUID specified for the new code system '" + terminology.getCodeSystem().getName() + "' : '" 
                  + terminology.getCodeSystem().getVUID() + "' is not unique to the import data");
         }
      }
      
      if (terminology.getCodeSystem().getVersion().getCodedConcepts() != null)
      {
         for (CodedConcept cc : terminology.getCodeSystem().getVersion().getCodedConcepts().getCodedConcept())
         {
            if (cc.getAction() == ActionType.ADD && cc.getVUID() != null)
            {
               if (Frills.getNidForVUID(cc.getVUID()).isPresent())
               {
                  throw new RuntimeException("The VUID specified for the new concept '" + cc.getName() + "' : '" + cc.getVUID() + "' is already in use");
               }
               else if (!vuidsInXmlFile_.add(cc.getVUID()))
               {
                  throw new RuntimeException("The VUID specified for the new concept '" + cc.getName() + "' : '" 
                        + cc.getVUID() + "' is not unique to the import data");
               }
            }
            if (cc.getDesignations() != null && cc.getDesignations().getDesignation() != null)
            {
               for (Designation d : cc.getDesignations().getDesignation())
               {
                  if (d.getAction() == ActionType.ADD && d.getVUID() != null)
                  {
                     if (Frills.getNidForVUID(d.getVUID()).isPresent())
                     {
                        throw new RuntimeException("The VUID specified for the new designation '" + d.getValueNew() + "' : '" + d.getVUID() + "' is already in use");
                     }
                     else if (!vuidsInXmlFile_.add(d.getVUID()))
                     {
                        throw new RuntimeException("The VUID specified for the new designation '" + d.getValueNew() + "' : '" 
                              + d.getVUID() + "' is not unique to the import data");
                     }
                  }
               }
            }
         }
      }
      
      if (terminology.getCodeSystem().getVersion().getMapSets() != null)
      {
         for (MapSet ms : terminology.getCodeSystem().getVersion().getMapSets().getMapSet())
         {
            if (ms.getAction() == ActionType.ADD && ms.getVUID() != null)
            {
               if (Frills.getNidForVUID(ms.getVUID()).isPresent())
               {
                  throw new RuntimeException("The VUID specified for the new mapset '" + ms.getName() + "' : '" + ms.getVUID() + "' is already in use");
               }
               else if (!vuidsInXmlFile_.add(ms.getVUID()))
               {
                  throw new RuntimeException("The VUID specified for the new mapset '" + ms.getName() + "' : '" 
                        + ms.getVUID() + "' is not unique to the import data");
               }
               
               if (ms.getDesignations() != null && ms.getDesignations().getDesignation() != null)
               {
                  for (MapSet.Designations.Designation d : ms.getDesignations().getDesignation())
                  {
                     if (d.getAction() == ActionType.ADD && d.getVUID() != null)
                     {
                        if (Frills.getNidForVUID(d.getVUID()).isPresent())
                        {
                           throw new RuntimeException("The VUID specified for the new mapset designation '" + d.getValueNew() + "' : '" + d.getVUID() 
                              + "' is already in use");
                        }
                        else if (!vuidsInXmlFile_.add(d.getVUID()))
                        {
                           throw new RuntimeException("The VUID specified for the new mapset designation '" + d.getValueNew() + "' : '" 
                                 + d.getVUID() + "' is not unique to the import data");
                        }
                     }
                  }
               }
            }
            if (ms.getMapEntries() != null && ms.getMapEntries().getMapEntry() != null)
            {
               for (MapEntry me : ms.getMapEntries().getMapEntry())
               {
                  if (me.getAction() == ActionType.ADD && me.getVUID() != null)
                  {
                     if (Frills.getNidForVUID(me.getVUID()).isPresent())
                     {
                        throw new RuntimeException("The VUID specified for the new map entry '" + me.getSourceCode() + "' : '" + ms.getVUID() + "' is already in use");
                     }
                     else if (!vuidsInXmlFile_.add(me.getVUID()))
                     {
                        throw new RuntimeException("The VUID specified for the new map entry '" + me.getSourceCode() + "' : '" 
                              + me.getVUID() + "' is not unique to the import data");
                     }
                  }
                  if (me.getDesignations() != null && me.getDesignations().getDesignation() != null)
                  {
                     for (MapEntry.Designations.Designation d : me.getDesignations().getDesignation())
                     {
                        if (d.getAction() == ActionType.ADD && d.getVUID() != null)
                        {
                           if (Frills.getNidForVUID(d.getVUID()).isPresent())
                           {
                              throw new RuntimeException("The VUID specified for the new mapentry designation '" + d.getValueNew() + "' : '" + d.getVUID() 
                                 + "' is already in use");
                           }
                           else if (!vuidsInXmlFile_.add(d.getVUID()))
                           {
                              throw new RuntimeException("The VUID specified for the new mapentry designation '" + d.getValueNew() + "' : '" 
                                    + d.getVUID() + "' is not unique to the import data");
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      
      if (terminology.getSubsets() != null)
      {
         for (Subset s : terminology.getSubsets().getSubset())
         {
            if (s.getAction() == ActionType.ADD && s.getVUID() != null)
            {
               if (Frills.getNidForVUID(s.getVUID()).isPresent())
               {
                  throw new RuntimeException("The VUID specified for the new subset '" + s.getName() + "' : '" + s.getVUID() + "' is already in use");
               }
               else if (!vuidsInXmlFile_.add(s.getVUID()))
               {
                  throw new RuntimeException("The VUID specified for the new subset '" + s.getName() + "' : '" 
                        + s.getVUID() + "' is not unique to the import data");
               }
            }
         }
      }
   }
   
   /**
    * Put any annotations and associations listed in the new section into our lists for UUID lookup.
    */
   private void populateNewProperties(Terminology terminology)
   {
      LOG.info("Checking for properties that need creation");
      
      if (terminology.getTypes() != null)
      {
         for (Type t : terminology.getTypes().getType())
         {
            String name = StringUtils.trim(t.getName());
            switch (t.getKind())
            {
               case DESIGNATION_TYPE:
                  throw new RuntimeException("New extended designations types aren't supported yet");
               case PROPERTY_TYPE:
                  this.annotations.addProperty(name);
                  break;
               case RELATIONSHIP_TYPE:
                  this.associations.addProperty(new PropertyAssociation(this.associations, name, name, null, name, false));   
                  break;
               default :
                  throw new RuntimeException("Unexepected error");
               
            }
         }
      }
   }
   
   /**
    * Put any Subsets listed in the new section into our lists for UUID lookup.
    */
   private void populateNewSubsets(Terminology terminology) throws IOException
   {
      LOG.info("Checking for subsets that need creation");
      
      if (terminology.getSubsets() != null)
      {
         for (Subset s : terminology.getSubsets().getSubset())
         {
            String name = StringUtils.trim(s.getName());
            switch (s.getAction())
            {
               case ADD:
                  this.subsets.addProperty(name);
                  
                  if (s.getVUID() != null)
                  {
                     this.vuidToSubsetMap.put(s.getVUID(), this.subsets.getProperty(name).getUUID());
                  }
                  break;
               case REMOVE: case NONE:
                  // no-op
                  break;
               case UPDATE:
                  throw new IOException("Update of subset is not supported: " + name);
               default :
                  throw new RuntimeException("Unexepected error");
            }
         }
      }
   }
   
   /**
    * Just loads the ones that were specified as new.
    */
   private void createNewProperties(Terminology terminology) throws Exception
   {
      //Have to turn off dupe detection, cause they were already entered once, above, in the populate code.
      ConverterUUID.disableUUIDMap = true;
      LOG.info("Checking for properties that need creation");
      sh.isaac.converters.sharedUtils.propertyTypes.PropertyType associations = new BPT_Associations("VHAT");
      sh.isaac.converters.sharedUtils.propertyTypes.PropertyType annotations = new BPT_Annotations("VHAT");
      
      if (terminology.getTypes() != null)
      {
         for (Type t : terminology.getTypes().getType())
         {
            String name = StringUtils.trim(t.getName());
            switch (t.getKind())
            {
               case DESIGNATION_TYPE:
                  throw new RuntimeException("New extended designations types aren't supported yet");
               case PROPERTY_TYPE:
                  annotations.addProperty(name);
                  break;
               case RELATIONSHIP_TYPE:
                  associations.addProperty(new PropertyAssociation(associations, name, name, null, name, false));   
                  break;
               default :
                  throw new RuntimeException("Unexepected error");
               
            }
         }
      }
      
      importUtil_.loadMetaDataItems(associations, null);
      importUtil_.loadMetaDataItems(annotations, null);
      ConverterUUID.disableUUIDMap = false;
   }
   
   private void createNewSubsets(Terminology terminology) throws IOException
   {
      LOG.info("Creating new Subsets");
      
      if (terminology.getSubsets() != null)
      {
         //create the new ones
         try
         {
            importUtil_.loadMetaDataItems(this.subsets, null);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unexpected error");
         }
         
         for (Subset s : terminology.getSubsets().getSubset())
         {
            String name = StringUtils.trim(s.getName());
            switch (s.getAction())
            {
               case ADD:
                  //add the vuid, now that the concept is there
                  Long vuid = s.getVUID() == null ? 
                        (this.vuidSupplier == null ? null : this.vuidSupplier.getAsLong())
                        : s.getVUID();
            
                  if (vuid != null)
                  {
                     this.vuidToSubsetMap.put(vuid, this.subsets.getProperty(name).getUUID());
                     importUtil_.addStaticStringAnnotation(ComponentReference.fromConcept(this.subsets.getProperty(name).getUUID()), vuid.toString(), 
                           MetaData.VUID____SOLOR.getPrimordialUuid(), Status.ACTIVE);
                  }
                  break;
               case REMOVE:
                  // add it into the subset map
                  this.subsets.addProperty(name);
                  importUtil_.createConcept(this.subsets.getProperty(name).getUUID(), null, Status.INACTIVE, null);
                  break;
               case NONE:
                  // add it into the subset map
                  this.subsets.addProperty(name);
                  break;
               default :
                  throw new RuntimeException("Unexepected error");
            }
         }
      }
   }
   
   
   private void headerCheck(Terminology terminology) throws IOException
   {
      LOG.info("Checking the file header");
      CodeSystem cs = terminology.getCodeSystem();
      if (cs.getAction() != null && cs.getAction() != ActionType.NONE)
      {
         throw new IOException("Code System must be null or 'none' for this importer");
      }
      
      if (StringUtils.isNotBlank(cs.getName()) && !cs.getName().equalsIgnoreCase("VHAT"))
      {
         throw new IOException("Code System Name should be blank or VHAT");
      }
      
      if (cs.getVersion().isAppend() != null && !cs.getVersion().isAppend().booleanValue())
      {
         throw new IOException("Append must be true if provided");
      }
      
      // TODO (later) ever need to handle preferred designation type?  I don't think you can change it during a delta update
      // currently, we don't even have / store this information, so need to look at that from a bigger picture
   }
   
   /**
    * @param terminology
    * @throws IOException 
    */
   private void requiredChecks(Terminology terminology) throws IOException
   {
      if (terminology.getCodeSystem() != null && terminology.getCodeSystem().getVersion() != null)
      {
         if (terminology.getCodeSystem().getVersion().getCodedConcepts() != null)
         {
            for (CodedConcept cc : terminology.getCodeSystem().getVersion().getCodedConcepts().getCodedConcept())
            {
               UUID conceptUUID = conceptChecks(cc);
               
               if (cc.getDesignations() != null)
               {
                  for (Designations.Designation d : cc.getDesignations().getDesignation())
                  {
                     designationChecks(d, cc, conceptUUID);
                  }
               }
               
               if (cc.getProperties() != null)
               {
                  for (Property p : cc.getProperties().getProperty())
                  {
                     propertyChecks(p, cc, conceptUUID);
                  }
               }
               
               if (cc.getRelationships() != null)
               {
                  for (Relationship r : cc.getRelationships().getRelationship())
                  {
                     if (this.associations.getProperty(StringUtils.trim(r.getTypeName())) == null)
                     {
                        this.associations.addProperty(new PropertyAssociation(null, StringUtils.trim(r.getTypeName()), null, null, "doesn't-matter", false));
                        if (!Get.conceptService().hasConcept(Get.identifierService()
                              .getNidForUuids(this.associations.getProperty(StringUtils.trim(r.getTypeName())).getUUID())))
                        {
                           throw new IOException("The association '" + StringUtils.trim(r.getTypeName()) + "' isn't in the system - from " + cc.getCode() 
                              + " and it wasn't listed as a new association.  Expected to find " + this.associations.getProperty(StringUtils.trim(r.getTypeName())).getUUID());
                        }
                     }
                     if (r.getAction() == null)
                     {
                        throw new IOException("Action must be provided on every relationship.  Missing on " + cc.getCode() + ":" 
                           + StringUtils.trim(r.getTypeName()));
                     }
                     if (r.getAction() == ActionType.REMOVE && r.isActive() == null)
                     {
                        r.setActive(false);
                     }

                     if (r.isActive() == null)
                     {
                        throw new IOException("Active must be provided on every relationship.  Missing on " + cc.getCode() + ":" 
                           + StringUtils.trim(r.getTypeName()));
                     }
                     
                     switch(r.getAction())
                     {
                        case ADD:
                           Optional<UUID> targetConcept = findConcept(StringUtils.trim(r.getNewTargetCode()));
                           if (StringUtils.isBlank(r.getNewTargetCode()) || !targetConcept.isPresent())
                           {
                              throw new IOException("New Target Code must be provided for new relationships.  Missing on " + 
                                 cc.getCode() + ":" + StringUtils.trim(r.getTypeName()));
                           }
                           if (conceptUUID != null && findAssociationSememe(conceptUUID, this.associations.getProperty(StringUtils.trim(r.getTypeName())).getUUID(), 
                                 targetConcept.get()).isPresent())
                           {
                              throw new IOException("Add was specified for the association." + 
                                 cc.getCode() + ":" + StringUtils.trim(r.getTypeName()) + ":" + r.getNewTargetCode() + " but is already seems to exist");
                           }
                           break;
                        case NONE:
                           //noop
                           break;
                        case REMOVE:
                        case UPDATE:
                           Optional<UUID> oldTarget = findConcept(r.getOldTargetCode());
                           if (StringUtils.isBlank(r.getOldTargetCode()) || !oldTarget.isPresent())
                           {
                              throw new IOException("Old Target Code must be provided for existing relationships.  Missing on " + 
                                 cc.getCode() + ":" + StringUtils.trim(r.getTypeName()));
                           }
                           if (!findAssociationSememe(conceptUUID, this.associations.getProperty(StringUtils.trim(r.getTypeName())).getUUID(), 
                              oldTarget.get()).isPresent())
                           {
                              throw new IOException("Can't locate existing association to update for .  Missing on " + 
                                 cc.getCode() + ":" + StringUtils.trim(r.getTypeName()) + ":" + r.getOldTargetCode());
                           }
                           break;
                     }
                  }
               }
            }
         }
         
         if (terminology.getCodeSystem().getVersion().getMapSets() != null && terminology.getCodeSystem().getVersion().getMapSets().getMapSet() != null)
         {
            if (terminology.getCodeSystem().getVersion().getMapSets().getMapSet().size() > 0 && this.associations.getProperty("has_parent") == null)
            {
               this.associations.addProperty(new PropertyAssociation(null, "has_parent", null, null, "doesn't-matter", false));
               if (!Get.conceptService().hasConcept(Get.identifierService()
                     .getNidForUuids(this.associations.getProperty("has_parent").getUUID())))
               {
                  throw new IOException("The association 'has_parent' isn't in the system - required for mapsets "
                     + " and it wasn't listed as a new association.  Expected to find " + this.associations.getProperty("has_parent").getUUID());
               }
            }
            for (MapSet ms : terminology.getCodeSystem().getVersion().getMapSets().getMapSet())
            {
               UUID mapsetUUID = conceptChecks(ms);
               
               if (ms.getDesignations() != null && ms.getDesignations().getDesignation() != null)
               {
                  for (sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets.MapSet.Designations.Designation d : 
                     ms.getDesignations().getDesignation())
                  {
                     designationChecks(d, ms, mapsetUUID);
                  }
               }
               
               if (ms.getProperties() != null)
               {
                  for (sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets.MapSet.Properties.Property p : 
                     ms.getProperties().getProperty())
                  {
                     propertyChecks(p, ms, mapsetUUID);
                  }
               }
               
               if (ms.getRelationships() != null && ms.getRelationships().getRelationship() != null && ms.getRelationships().getRelationship().size() > 0)
               {
                  throw new IOException("Relationships are not supported on mapsets.  Mapset '" + ms.getName() + "'");
               }
               
               if (StringUtils.isBlank(ms.getName()))
               {
                  throw new IOException("Mapsets must have names - not found on " + ms.getCode());
               }
               
               for (MapEntry me : ms.getMapEntries().getMapEntry())
               {
                  if (me.getDesignations() != null && me.getDesignations().getDesignation() != null && me.getDesignations().getDesignation().size() > 0)
                  {
                     throw new IOException("Designations are not supported on map entries!");
                  }
                  
                  if (me.getRelationships() != null && me.getRelationships().getRelationship() != null && me.getRelationships().getRelationship().size() > 0)
                  {
                     throw new IOException("Designations are not supported on map entries!");
                  }
                  
                  if (me.getProperties() != null && me.getProperties().getProperty() != null)
                  {
                     String gemFlag = null;
                     for (Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties.Property p : 
                        me.getProperties().getProperty())
                     {
                        if (!p.getTypeName().equals("GEM_Flags"))
                        {
                           throw new IOException("The only property supported on mapsets is 'GEM_Flags'.  '" + p.getTypeName() + "' is not supported");
                        }
                        else
                        {
                           if (gemFlag != null)
                           {
                              throw new IOException("Only expect one gem flag per map entry");
                           }
                           else
                           {
                              gemFlag = p.getValueNew();
                           }
                        }
                     }
                  }
                  
                  if (me.getAction() == null)
                  {
                     throw new IOException("Action is required on all map entries!");
                  }

                  if (me.getAction() == ActionType.ADD)
                  {
                     if (StringUtils.isBlank(me.getSourceCode()))
                     {
                        throw new IOException("Source code is required on all add map entries");
                     }
                     
                     if (StringUtils.isBlank(me.getTargetCode()))
                     {
                        throw new IOException("Target code is required on all add map entries");
                     }
                     if (me.getVUID() == null && this.vuidSupplier == null)
                     {
                        throw new IOException("No VUID was supplied for map entry '" + me.getSourceCode() + ":" + me.getTargetCode() 
                        + "' and vuid generation was not requested.");
                     }
                     
                     if (!findConcept(me.getSourceCode()).isPresent() && !this.conceptsToBeCreated.contains(me.getSourceCode()))
                     {
                        throw new IOException("The source concept '" + me.getSourceCode() + "' doesn't exist for creating the mapset " + ms.getName());
                     }
                     
                     if (!findConcept(me.getTargetCode()).isPresent() && !this.conceptsToBeCreated.contains(me.getTargetCode()))
                     {
                        throw new IOException("The source concept '" + me.getSourceCode() + "' doesn't exist for creating the mapset " + ms.getName());
                     }
                     
                     if (mapsetUUID != null && me.getVUID() != null &&  Get.identifierService().hasUuid(createNewMapItemUUID(mapsetUUID, me.getVUID().toString())) 
                           && Get.assemblageService().hasSemantic(Get.identifierService().getNidForUuids(createNewMapItemUUID(mapsetUUID, me.getVUID().toString()))))
                     {
                        throw new IOException("The map entry '" + me.getVUID() + "' already exists");
                     }
                  }
                  
                  if (me.getAction() == ActionType.REMOVE || me.getAction() == ActionType.UPDATE)
                  {
                     if (me.getVUID() == null)
                     {
                        throw new IOException("VUID is required on all remove or update vuid entries");
                     }
                     
                     if (!Get.identifierService().hasUuid(createNewMapItemUUID(mapsetUUID, me.getVUID().toString())) 
                           || Get.assemblageService().hasSemantic(Get.identifierService().getNidForUuids(createNewMapItemUUID(mapsetUUID, me.getVUID().toString()))))
                     {
                        throw new IOException("The map entry for '" + me.getVUID() + "' could not be found");
                     }
                     
                     if (me.getAction() == ActionType.UPDATE)
                     {
                        UUID existingSourceUUID = Get.identifierService().getUuidPrimordialForNid(Get.assemblageService().getSemanticChronology(
                              Get.identifierService().getNidForUuids(createNewMapItemUUID(mapsetUUID, me.getVUID().toString()))).getReferencedComponentNid()).get();
                        if (existingSourceUUID.equals(findConcept(me.getSourceCode()).get()))
                        {
                           throw new IOException("Changing the source concept of a map entry isn't allowed.  Retire the map entry, create a new one.");
                        }
                     }
                  }
               }
            }
         }
         
      }
   }
   
   /**
    * @return may return null, if no code was specified in the concept
    * @throws IOException
    */
   private UUID conceptChecks(ConceptType concept) throws IOException
   {
      UUID conceptUUID = null;
      final String typeLabel;
      if (concept instanceof Terminology.CodeSystem.Version.CodedConcepts.CodedConcept)
      {
         typeLabel = "concept";
      }
      else if (concept instanceof Terminology.CodeSystem.Version.MapSets.MapSet)
      {
         typeLabel = "mapset";
      }
      else
      {
         typeLabel = "unexpected";
      }
      if (concept.getAction() == null)
      {
         throw new IOException("Action must be provided on every " + typeLabel + ".  Missing on " + concept.getCode());
      }
      if (concept.getAction() == ActionType.REMOVE && concept.isActive() == null)
      {
         concept.setActive(false);
      }
      if (concept.isActive() == null)
      {
         throw new IOException("Active must be provided on every " + typeLabel + ".  Missing on " + concept.getCode());
      }
      if ((concept.getAction() == ActionType.UPDATE || concept.getAction() == ActionType.REMOVE || concept.getAction() == ActionType.NONE) 
         && StringUtils.isBlank(concept.getCode()))
      {
         throw new IOException("Concept code must be provided on every " + typeLabel + " where action is update or remove or none.");
      }
      else if (concept.getAction() != ActionType.ADD)
      {
         conceptUUID = findConcept(concept.getCode()).orElseThrow(() -> new RuntimeException("Cannot locate " + typeLabel + " for code '" + concept.getCode() + "'"));
      }
      if (concept.getAction() == ActionType.ADD)
      {
         if (StringUtils.isBlank(concept.getCode()) && concept.getVUID() == null && this.vuidSupplier == null)
         {
            throw new IOException("No code or vuid was supplied, and generate vuids was not requested for " + concept.getName());
         }
         
         String code = StringUtils.isBlank(concept.getCode()) ? (concept.getVUID() == null ? null : concept.getVUID().toString()) : concept.getCode();
         if (StringUtils.isNotBlank(code) && findConcept(code).isPresent())
         {
            throw new IOException("Add was specified for the " + typeLabel + " '" + code + "' but that " + typeLabel + " already exists!");
         }
         if (StringUtils.isNotBlank(code))
         {
            this.conceptsToBeCreated.add(code);
            conceptUUID = createNewConceptUuid(code);
         }
      }
      return conceptUUID;
   }
   
   private void designationChecks(DesignationType d, ConceptType parentConcept, UUID parentConceptUUID) throws IOException
   {
      if (d.getAction() == null)
      {
         throw new IOException("Action must be provided on every designation.  Missing on " + parentConcept.getCode() + ":" + d.getCode());
      }
      if (d.getAction() == ActionType.REMOVE && d.isActive() == null)
      {
         d.setActive(false);
      }
      if (d.isActive() == null && StringUtils.isBlank(d.getMoveFromConceptCode()))
      {
         throw new IOException("Active must be provided on designations in this case.  Missing on " + parentConcept.getCode() + ":" + d.getCode());
      }
      if (StringUtils.isNotBlank(d.getTypeName()))
      {
         if (extendedDescriptionTypeNameMap.get(d.getTypeName().toLowerCase()) == null)
         {
            throw new IOException("Unexpected TypeName on " + parentConcept.getCode() + ":" + d.getCode() + ": " + d.getTypeName());
         }
      }
      else
      {
         //type is required on add
         if (d.getAction() == ActionType.ADD)
         {
            throw new IOException("Missing TypeName on " + parentConcept.getCode() + ":" + d.getCode());
         }
      }
      
      if ((d.getAction() == ActionType.ADD || d.getAction() == ActionType.NONE || d.getAction() == ActionType.REMOVE) 
            && StringUtils.isNotBlank(d.getMoveFromConceptCode()))
      {
         throw new IOException("Move From Concept Code should only be used with action type of UPDATE");
      }
      
      UUID descriptionUUID = null;
      
      if (d.getAction() == ActionType.REMOVE || d.getAction() == ActionType.UPDATE || d.getAction() == ActionType.NONE)
      {
         if (StringUtils.isBlank(d.getCode()))
         {
            throw new IOException("The designation '" + d.getTypeName() + "' doesn't have a code - from " + parentConcept.getCode()+ ":" 
               + d.getCode());
         }
         if (d.getAction() == ActionType.UPDATE && StringUtils.isNotBlank(d.getMoveFromConceptCode()))
         {
            Optional<UUID> donerConcept = findConcept(d.getMoveFromConceptCode());
            if (!donerConcept.isPresent())
            {
               throw new IOException("Cannot locate MoveFromConceptCode '" + d.getMoveFromConceptCode() + "' for designation '"
                  + d.getCode() + "' on concept:" +  parentConcept.getCode()+ ":" + d.getCode());
            }
            Optional<UUID> description = findDescription(donerConcept.get(), d.getCode());
            if (!description.isPresent())
            {
               throw new IOException("The designation '" + d.getCode() + "' doesn't seem to exist on doner concept:" +  d.getMoveFromConceptCode() 
                  + ":"+ d.getCode());
            }
            descriptionUUID = description.get();
         }
         else 
         {
            Optional<UUID> description = findDescription(parentConceptUUID, d.getCode());
            if (!description.isPresent())
            {
               throw new IOException("The designation '" + d.getCode() + "' doesn't seem to exist on concept:" +  parentConcept.getCode()+ ":" 
                     + d.getCode());
            }
            descriptionUUID = description.get();
         }
      }
      
      if (d.getAction() == ActionType.ADD)
      {
         if (StringUtils.isBlank(d.getCode()) && d.getVUID() == null && this.vuidSupplier == null)
         {
            throw new IOException("No code or vuid was supplied, and generate vuids was not requested for a designation");
         }
         
         String code = StringUtils.isBlank(d.getCode()) ? (d.getVUID() == null ? null : d.getVUID().toString()) : d.getCode();
         if (StringUtils.isNotBlank(code) && findDescription(parentConceptUUID, code).isPresent())
         {
            throw new IOException("Add was specified for the designation '" + code + "' but that designation already exists!");
         }
         if (StringUtils.isNotBlank(code))
         {
            descriptionUUID = createNewDescriptionUuid(parentConceptUUID, code);
         }
      }
      
      if (d instanceof Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation)
      {
         Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation cd = 
               (Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation)d;
         if (cd.getProperties() != null)
         {
            for ( PropertyType p : cd.getProperties().getProperty())
            {
               if (this.annotations.getProperty(p.getTypeName()) == null)
               {
                  //add it, so we have the UUID mapping, but we don't need to create it, since it should already exist
                  this.annotations.addProperty(p.getTypeName());
                  if (!Get.conceptService().hasConcept(Get.identifierService()
                        .getNidForUuids(this.annotations.getProperty(p.getTypeName()).getUUID())))
                  {
                     throw new IOException("The property '" + p.getTypeName() + "' isn't in the system - from " + parentConcept.getCode() + ":" 
                        + d.getCode() + " and it wasn't listed as a new property");
                  }
               }
               if (p.getAction() == null)
               {
                  throw new IOException("Action must be provided on every property.  Missing on " + parentConcept.getCode() + ":" 
                     + d.getCode() + ":" + p.getTypeName());
               }
               if (p.getAction() == ActionType.REMOVE && p.isActive() == null)
               {
                  p.setActive(false);
               }
   
               if (p.isActive() == null)
               {
                  throw new IOException("Active must be provided on every property.  Missing on " + parentConcept.getCode() + ":" 
                     + d.getCode() + ":" + p.getTypeName());
               }
               
               if (p.getAction() == ActionType.REMOVE || p.getAction() == ActionType.UPDATE)
               {
                  if (StringUtils.isBlank(p.getValueOld()))
                  {
                     throw new IOException("The property '" + p.getTypeName() + "' doesn't have a ValueOld - from " + parentConcept.getCode()+ ":" 
                        + d.getCode());
                  }
                  if (!findPropertySememe(descriptionUUID, this.annotations.getProperty(p.getTypeName()).getUUID(), p.getValueOld()).isPresent())
                  {
                     throw new IOException("The property '" + p.getTypeName() + "' with an old Value of ' " + p.getValueOld() 
                        + "' doesn't seem to exist on concept:" +  parentConcept.getCode()+ ":" 
                           + d.getCode());
                  }
               }
               
               if (p.getAction() == ActionType.ADD && findPropertySememe(descriptionUUID, this.annotations.getProperty(p.getTypeName()).getUUID(),
                     p.getValueNew()).isPresent())
               {
                  throw new IOException("Add was specified for the property '" + p.getTypeName() + "' with an new Value of ' " + p.getValueNew() 
                     + "' but that property already exists on :" +  parentConcept.getCode()+ ":"+ d.getCode());
               }
               
            }
         }
         
         if (cd.getSubsetMemberships() != null)
         {
            for (SubsetMembership sm : cd.getSubsetMemberships().getSubsetMembership())
            {
               if (this.vuidToSubsetMap.get(sm.getVUID()) == null)
               {
                  //add it, so we have the UUID mapping, but we don't need to create it, since it should already exist
                  Optional<Integer> subsetNid = Frills.getNidForVUID(sm.getVUID());
                  if (!subsetNid.isPresent())
                  {
                     throw new IOException("The subset '" + sm.getVUID() + "' isn't in the system - from " + parentConcept.getCode() + ":" 
                        + d.getCode() + " and it wasn't listed as a new subset");
                  }
                  else
                  {
                     this.vuidToSubsetMap.put(sm.getVUID(), Get.identifierService().getUuidPrimordialForNid(subsetNid.get()).get());
                  }
               }
               if (sm.getAction() == null)
               {
                  throw new IOException("Action must be provided on every subset membership.  Missing on " + parentConcept.getCode() + ":" 
                     + d.getCode() + ":" + sm.getVUID());
               }
               if (sm.getAction() == ActionType.REMOVE && sm.isActive() == null)
               {
                  sm.setActive(false);
               }
   
               if (sm.isActive() == null)
               {
                  throw new IOException("Active must be provided on every subset membership.  Missing on " + parentConcept.getCode() + ":" 
                     + d.getCode() + ":" + sm.getVUID());
               }
               
               boolean membershipExists = descriptionUUID == null ? false : 
                  Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(Get.identifierService().getNidForUuids(descriptionUUID), 
                     Get.identifierService().getNidForUuids(this.vuidToSubsetMap.get(sm.getVUID()))).findAny().isPresent();
               if (sm.getAction() == ActionType.ADD && membershipExists)
               {
                  throw new IOException("Add was specified for a subset membership, but it appears to already exist for " + parentConcept.getCode() + ":" 
                        + d.getCode() + ":" + sm.getVUID());
               }
               else if ((sm.getAction() == ActionType.UPDATE || sm.getAction() == ActionType.REMOVE) && !membershipExists)
               {
                  throw new IOException("Remove or update was specified for a subset membership, but the membership doesn't currently exist for "
                        + parentConcept.getCode() + ":"+ d.getCode() + ":" + sm.getVUID());
               }
            }
         }
      }
   }
   
   private void propertyChecks(PropertyType p, ConceptType parentConcept, UUID parentConceptUUID) throws IOException
   {
      if (this.annotations.getProperty(p.getTypeName()) == null)
      {
         this.annotations.addProperty(p.getTypeName());
         if (!Get.conceptService().hasConcept(Get.identifierService()
               .getNidForUuids(this.annotations.getProperty(p.getTypeName()).getUUID())))
         {
            throw new IOException("The property '" + p.getTypeName() + "' isn't in the system - from " + parentConcept.getCode() 
               + " and it wasn't listed as a new property.  Expected to find " + this.annotations.getProperty(p.getTypeName()).getUUID());
         }
      }
      if (p.getAction() == null)
      {
         throw new IOException("Action must be provided on every property.  Missing on " + parentConcept.getCode() + ":" 
            + p.getTypeName());
      }
      if (p.getAction() == ActionType.REMOVE && p.isActive() == null)
      {
         p.setActive(false);
      }

      if (p.isActive() == null)
      {
         throw new IOException("Active must be provided on every property.  Missing on " + parentConcept.getCode() + ":" 
            + p.getTypeName());
      }
      if (p.getAction() == ActionType.ADD && StringUtils.isBlank(p.getValueNew()))
      {
         throw new IOException("The property '" + p.getTypeName() + "' doesn't have a ValueNew - from " + parentConcept.getCode());
      }
      if (p.getAction() == ActionType.REMOVE || p.getAction() == ActionType.UPDATE)
      {
         if (StringUtils.isBlank(p.getValueOld()))
         {
            throw new IOException("The property '" + p.getTypeName() + "' doesn't have a ValueOld - from " + parentConcept.getCode());
         }
         if (!findPropertySememe(parentConceptUUID, this.annotations.getProperty(p.getTypeName()).getUUID(), p.getValueOld()).isPresent())
         {
            throw new IOException("The property '" + p.getTypeName() + "' with an old Value of ' " + p.getValueOld() 
               + "' doesn't seem to exist on concept:" +  parentConcept.getCode());
         }
      }
      
      if (p.getAction() == ActionType.ADD && findPropertySememe(parentConceptUUID, this.annotations.getProperty(p.getTypeName()).getUUID(),
            p.getValueNew()).isPresent())
      {
         throw new IOException("Add was specified for the property '" + p.getTypeName() + "' with an new Value of ' " + p.getValueNew() 
            + "' but that property already exists on :" +  parentConcept.getCode());
      }
   }

   private void loadConcepts(CodedConcepts codedConcepts) throws IOException
   {
      if (codedConcepts != null)
      {
         LOG.info("Loading "  + codedConcepts.getCodedConcept().size() + " Concepts");
         
         for (CodedConcept cc : codedConcepts.getCodedConcept())
         {
            ComponentReference concept = null;
            switch (cc.getAction())
            {
               case ADD:
                  String vuid = cc.getVUID() == null ? 
                        (this.vuidSupplier == null ? null : this.vuidSupplier.getAsLong() + "")
                        : cc.getVUID().toString();
                  
                  String code = StringUtils.isBlank(cc.getCode()) ? vuid : cc.getCode();
                  
                  if (StringUtils.isBlank(code))
                  {
                     throw new RuntimeException("No code supplied, and vuid generation is disabled!");
                  }
                  concept = ComponentReference.fromConcept(importUtil_.createConcept(createNewConceptUuid(code), null, 
                              cc.isActive() ? Status.ACTIVE : Status.INACTIVE, null));
                        
                  if (StringUtils.isNotBlank(vuid))
                  {
                     importUtil_.addStaticStringAnnotation(concept, vuid, MetaData.VUID____SOLOR.getPrimordialUuid(), Status.ACTIVE);
                  }
                  
                  if (StringUtils.isNotBlank(code))
                  {
                     importUtil_.addStaticStringAnnotation(concept, code, MetaData.CODE____SOLOR.getPrimordialUuid(), Status.ACTIVE);
                  }
                  break;
               case NONE:
                  //noop
                  break;
               case REMOVE:
                  concept = ComponentReference.fromConcept(importUtil_.createConcept(findConcept(cc.getCode()).get(), null, Status.INACTIVE, null));
                  for (Chronology o : recursiveRetireNested(concept.getPrimordialUuid()))
                  {
                     importUtil_.storeManualUpdate(o);
                  }
                  break;
               case UPDATE:
                  //We could, potentially support updating vuid, but the current system doesnt.
                  //so we only process activate / inactivate changes here.
                  concept = ComponentReference.fromConcept(importUtil_.createConcept(findConcept(cc.getCode()).get(), null, 
                     cc.isActive() ? Status.ACTIVE : Status.INACTIVE, null));
                  break;
               default :
                  throw new RuntimeException("Unexpected error");
            }
            if (concept == null)
            {
               concept = ComponentReference.fromConcept(findConcept(cc.getCode()).get());
            }
            loadDesignations(concept, cc.getDesignations());
            loadConceptProperties(concept, cc.getProperties());
            loadRelationships(concept, cc.getRelationships());
         }
      }
   }

   private UUID createNewConceptUuid(String codeId)
   {
      return ConverterUUID.createNamespaceUUIDFromString("code:" + codeId, true);
   }
   
   private UUID createNewDescriptionUuid(UUID concept, String descriptionId)
   {
      if (concept == null || StringUtils.isBlank(descriptionId))
      {
         return null;
      }
      return ConverterUUID.createNamespaceUUIDFromString("description:" + concept.toString() + ":" + descriptionId, true);
   }
   
   private UUID createNewMapItemUUID(UUID mapSetUUID, String mapItemVuid)
   {
      return ConverterUUID.createNamespaceUUIDFromString("mapSetUuid:" + mapSetUUID + "mapItemVuid:" + mapItemVuid, true);
   }

   /**
    * @param properties
    */
   private void loadConceptProperties(ComponentReference concept, 
         sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Properties properties)
   {
      if (properties != null)
      {
         for (Property p : properties.getProperty())
         {
            handleProperty(concept, p.getTypeName(),  p.getValueOld(),  p.getValueNew(),  p.isActive(),  p.getAction());
         }
      }
   }
   
   private void loadConceptProperties(ComponentReference concept, 
         sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets.MapSet.Properties properties)
      {
         if (properties != null)
         {
            for (sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets.MapSet.Properties.Property p : properties.getProperty())
            {
               handleProperty(concept, p.getTypeName(),  p.getValueOld(),  p.getValueNew(),  p.isActive(),  p.getAction());
            }
         }
      }
   
   private void handleProperty(ComponentReference component, String propertyName, String oldValue, String newValue, boolean isActive, ActionType action)
   {
      newValue = StringUtils.trim(newValue);
      
      switch (action)
      {
         case ADD:
            importUtil_.addStringAnnotation(component, newValue, this.annotations.getProperty(propertyName).getUUID(), 
               isActive ? Status.ACTIVE : Status.INACTIVE);
            break;
         case NONE:
            //noop
            break;
         case REMOVE:
            // REMOVE directive takes precedence. Explicitely set active=false, and fall-through
            isActive = false;
         case UPDATE:
            //These cases are a bit tricky, because the UUID calculated for the sememe was based on the value.  If the value
            //only changed once, you could use old value to find it, but, if it changes twice, you can no longer calculate back to the UUID.
            //So, we will have to match the oldvalue text string directly to the value, to find the right property.
            Optional<UUID> oldProperty = findPropertySememe(component.getPrimordialUuid(), this.annotations.getProperty(propertyName).getUUID(), oldValue);
            //we tested this lookup in an earlier error checking pass above, it shouldn't come back null.
            if (!oldProperty.isPresent())
            {
               throw new RuntimeException("oops");
            }
            
            SemanticChronology sc = Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(oldProperty.get()));
            if (sc.getVersionType() == VersionType.STRING)
            {
               //not allowing them to set the value to empty, just assume they only meant to change status in the case where new value is missing
               MutableStringVersion mss = sc.createMutableVersion(isActive ? Status.ACTIVE : Status.INACTIVE, this.editCoordinate);
               mss.setString(StringUtils.isBlank(newValue) ? oldValue : newValue);
            }
            else if (sc.getVersionType() == VersionType.DYNAMIC)
            {
               MutableDynamicVersion<?> mds = sc.createMutableVersion(isActive ? Status.ACTIVE : Status.INACTIVE, this.editCoordinate);
               if (mds.getDynamicUsageDescription().getColumnInfo().length != 1 || 
                     mds.getDynamicUsageDescription().getColumnInfo()[0].getColumnDataType() != DynamicDataType.STRING)
               {
                  throw new RuntimeException("Unexpected dynamic sememe data config!");
               }
               else
               {
                  mds.setData(new DynamicData[] {new DynamicStringImpl(StringUtils.isBlank(newValue) ? oldValue : newValue)});
               }
            }
            else
            {
               throw new RuntimeException("Unexpected sememe type!");
            }
            importUtil_.storeManualUpdate(sc);
            break;
         default :
            throw new RuntimeException("Unexepected error");
      }
   }
   
   private Optional<UUID> findPropertySememe(UUID referencedComponent, UUID propertyType, String propertyValue)
   {
      if (referencedComponent == null)
      {
         return Optional.empty();
      }
      return Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(Get.identifierService().getNidForUuids(referencedComponent), 
         Get.identifierService().getNidForUuids(propertyType)).filter(semantic ->
         {
            if (semantic.getVersionType() == VersionType.STRING)
            {
               LatestVersion<StringVersionImpl> sv = semantic.getLatestVersion(this.readCoordinate);
               if (sv.isPresent())
               {
                  return propertyValue.equals(sv.get().getString());
               }
            }
            else if (semantic.getVersionType() == VersionType.DYNAMIC)
            {
               LatestVersion<DynamicImpl> sv = semantic.getLatestVersion(this.readCoordinate);
               if (sv.isPresent() && sv.get().getDynamicUsageDescription().getColumnInfo().length == 1 && 
                     sv.get().getDynamicUsageDescription().getColumnInfo()[0].getColumnDataType() == DynamicDataType.STRING)
               {
                  return propertyValue.equals(sv.get().getData()[0].dataToString());
               }
            }
            return false;
         }).findFirst().<UUID>map(sememe -> 
         {
            return Get.identifierService().getUuidPrimordialForNid(sememe.getNid()).get();
         });
   }
   
   private Optional<UUID> findAssociationSememe(UUID sourceConcept, UUID associationType, UUID targetConcept)
   {
      return Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(Get.identifierService().getNidForUuids(sourceConcept), 
         Get.identifierService().getNidForUuids(associationType)).filter(sememe ->
         {
            if (sememe.getVersionType() == VersionType.DYNAMIC)
            {
               LatestVersion<DynamicImpl> sv = sememe.getLatestVersion(this.readCoordinate);
               if (sv.isPresent() && sv.get().getDynamicUsageDescription().getColumnInfo().length == 1 && 
                     sv.get().getDynamicUsageDescription().getColumnInfo()[0].getColumnDataType() == DynamicDataType.UUID)
               {
                  return  targetConcept.equals(((DynamicUUID) sv.get().getData()[0]).getDataUUID());
               }
            }
            return false;
         }).findFirst().<UUID>map(sememe -> 
         {
            return Get.identifierService().getUuidPrimordialForNid(sememe.getNid()).get();
         });
   }
   
   private void loadDesignations(ComponentReference concept, Designations designations)
   {
      if (designations != null)
      {
         for (Designation d : designations.getDesignation())
         {
            loadDesignation(concept, d);
         }
      }
   }
   
   private void loadDesignations(ComponentReference concept, Terminology.CodeSystem.Version.MapSets.MapSet.Designations designations)
   {
      if (designations != null)
      {
         for (sh.isaac.misc.constants.terminology.data.Terminology.CodeSystem.Version.MapSets.MapSet.Designations.Designation d : designations.getDesignation())
         {
            loadDesignation(concept, d);
         }
      }
   }

   /**
    * @param designations
    */
   private void loadDesignation(ComponentReference concept, DesignationType d)
   {
      ComponentReference descRef = null;
      
      String newValue = StringUtils.trim(d.getValueNew());
      
      switch (d.getAction())
      {
         case ADD:
            
            String vuid = d.getVUID() == null ? 
                  (this.vuidSupplier == null ? null : this.vuidSupplier.getAsLong() + "")
                  : d.getVUID().toString();
                  
            String code = StringUtils.isBlank(d.getCode()) ? vuid : d.getCode();
            
            descRef = ComponentReference.fromChronology(importUtil_.addDescription(concept, createNewDescriptionUuid(concept.getPrimordialUuid(), code), 
                  newValue, DescriptionType.REGULAR_NAME, false, extendedDescriptionTypeNameMap.get(d.getTypeName().toLowerCase()), 
               d.isActive() ? Status.ACTIVE : Status.INACTIVE));
            
            
            if (StringUtils.isNotBlank(vuid))
            {
               importUtil_.addStaticStringAnnotation(descRef, vuid, MetaData.VUID____SOLOR.getPrimordialUuid(), Status.ACTIVE);
            }
            
            if (StringUtils.isNotBlank(code))
            {
               importUtil_.addStaticStringAnnotation(descRef, code, MetaData.CODE____SOLOR.getPrimordialUuid(), Status.ACTIVE);
            }
            
            break;
         case NONE:
            //noop
            break;
         case REMOVE:
            // Here, we will iterate through the nested sememes, setting all to have a status 
            // of 'false' and then fall-through to the UPDATE clause to handle setting the status
            // of the designation itself
            // REMOVE directive takes precedence over Active element 
            // Explicitely set active=false, and fall-through
            d.setActive(false);
            // If the designation is inactivated/removed, set all nested sememes to inactive
            Optional<UUID> oldD = findDescription(concept.getPrimordialUuid(), d.getCode());
            //we tested this lookup in an earlier error checking pass above, it shouldn't come back null.
            if (!oldD.isPresent())
            {
               throw new RuntimeException("Unexected failure to chronology for description sememe " + oldD.get());
            }
            
            SemanticChronology sememeChronology = Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(oldD.get()));
            
            LatestVersion<DescriptionVersion> latestVersion = sememeChronology.getLatestVersion(this.readCoordinate);
            if (!latestVersion.isPresent())
            {
               throw new RuntimeException("Unexected failure to load latest version of description sememe " + oldD.get());
            }
            
            descRef = ComponentReference.fromChronology(sememeChronology);
            if (sememeChronology.getVersionType() == VersionType.DESCRIPTION)  //TODO dan asks, how could it possibly be anything else?  Why is this check here?
            {
               for (Chronology o : recursiveRetireNested(sememeChronology.getPrimordialUuid()))
               {
                  importUtil_.storeManualUpdate(o);
               }
            }
            // No break, fall-through for update to the designation itself
         case UPDATE:
            if (StringUtils.isBlank(d.getMoveFromConceptCode()))
            {
               Optional<UUID> oldDescription = findDescription(concept.getPrimordialUuid(), d.getCode());
               //we tested this lookup in an earlier error checking pass above, it shouldn't come back null.
               if (!oldDescription.isPresent())
               {
                  throw new RuntimeException("Unexected failure to chronology for description sememe " + oldDescription.get());
               }
               
               SemanticChronology sc = Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(oldDescription.get()));
               
               LatestVersion<DescriptionVersion> latest = sc.getLatestVersion(this.readCoordinate);
               if (!latest.isPresent())
               {
                  throw new RuntimeException("Unexected failure to load latest version of description sememe " + oldDescription.get());
               }
               
               descRef = ComponentReference.fromChronology(sc);
               if (sc.getVersionType() == VersionType.DESCRIPTION)
               {
                  // Get existing active description extended type
                  Optional<UUID> existingDescriptionActiveExtendedTypeUuidOptional = Frills.getDescriptionExtendedTypeConcept(this.readCoordinate, descRef.getNid(), false);
                  // Get existing inactive description extended type only if active description extended type not present
                  Optional<UUID> existingDescriptionInactiveExtendedTypeUuidOptional = existingDescriptionActiveExtendedTypeUuidOptional.isPresent() ? Optional.empty() 
                        : Frills.getDescriptionExtendedTypeConcept(this.readCoordinate, descRef.getNid(), true); 
                  // Get existing description extended type, active if extant, otherwise inactive if extant
                  Optional<UUID> existingDescriptionExtendedTypeToUseUuidOptional = existingDescriptionActiveExtendedTypeUuidOptional.isPresent() ? 
                        existingDescriptionActiveExtendedTypeUuidOptional 
                        : (existingDescriptionInactiveExtendedTypeUuidOptional.isPresent() ? existingDescriptionInactiveExtendedTypeUuidOptional 
                              : Optional.empty());

                  // Each VHAT description should have an extended type
                  Optional<SemanticChronology> existingDescriptionExtendedTypeAnnotationSememe =
                        Frills.getAnnotationSememe(
                              descRef.getNid(), 
                              DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getNid());
                  if (! existingDescriptionExtendedTypeAnnotationSememe.isPresent()) {
                     LOG.error("Existing description {} has no extended type", descRef.getPrimordialUuid());
                  }

                  boolean checkForAndActivateRetiredDescriptionExtendedTypeAnnotationSememe = false;
                  if (StringUtils.isBlank(d.getTypeName())) {
                     checkForAndActivateRetiredDescriptionExtendedTypeAnnotationSememe = true;
                  } else if (d.getAction() != ActionType.REMOVE) { 
                     //No point in processing extended type info if they did a REMOVE, and may cause a duplicate edit with the recursive retire, above.
                     // Get extendedDescriptionTypeNameFromData from extendedDescriptionTypeNameMap
                     UUID extendedDescriptionTypeFromData = extendedDescriptionTypeNameMap.get(d.getTypeName().trim().toLowerCase());
                     
                     if (extendedDescriptionTypeFromData != null) {
                        // Found valid description extended type in imported data, so compare to active one (if any) in db
                        if (existingDescriptionExtendedTypeToUseUuidOptional.isPresent()) {
                           // Check if description extended type from loaded data matches existing active description extended type in db
                           if (existingDescriptionExtendedTypeToUseUuidOptional.get().equals(extendedDescriptionTypeFromData)
                                 && existingDescriptionActiveExtendedTypeUuidOptional.isPresent()) {
                              // loaded data equals db so ignore
                              checkForAndActivateRetiredDescriptionExtendedTypeAnnotationSememe = true;
                           } else {
                              // description extended type from loaded data does not match existing active description extended type in db, so update existing sememe
                              SemanticChronology existingDescriptionActiveExtendedTypeSemanticChronology = 
                                 existingDescriptionExtendedTypeAnnotationSememe.get();
                              DynamicImpl newDescriptionActiveExtendedTypeSememeVersion = 
                                    existingDescriptionActiveExtendedTypeSemanticChronology.createMutableVersion(Status.ACTIVE, this.editCoordinate);
                              newDescriptionActiveExtendedTypeSememeVersion.setData(new DynamicData[] { new DynamicUUIDImpl(extendedDescriptionTypeFromData) });
                              importUtil_.storeManualUpdate(existingDescriptionActiveExtendedTypeSemanticChronology);
                           }
                        } else {
                           // There is no existing description extended type on the description in the db so add it
                           // TODO this should never happen, as each VHAT description should be created with an extended type
                           importUtil_.addAnnotation(
                                 descRef,
                                 /*uuidForCreatedAnnotation*/ null,
                                 new DynamicData[] { new DynamicUUIDImpl(extendedDescriptionTypeFromData) },
                                 DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getPrimordialUuid(),
                                 Status.ACTIVE,
                                 /*time*/ null,
                                 Get.identifierService().getUuidPrimordialForNid(this.editCoordinate.getModuleNid()).get());
                        }
                     } else {
                        String msg = "Encountered unexpected description extended type name " + d.getTypeName() + ". Expected one of " 
                              + Arrays.toString(extendedDescriptionTypeNameMap.keySet().toArray());
                        LOG.error(msg);
                        throw new RuntimeException(msg);
                     }
                  }
                  
                  //Don't do this if we fell through from REMOVE, because that will have put in a retire edit.
                  if (checkForAndActivateRetiredDescriptionExtendedTypeAnnotationSememe && d.getAction() != ActionType.REMOVE) {
                     // Just in case the description extended type has been inappropriately retired, unretire it
                     if (existingDescriptionExtendedTypeAnnotationSememe.isPresent()) {
                        SemanticChronology existingDescriptionExtendedTypeSemanticChronology = 
                           existingDescriptionExtendedTypeAnnotationSememe.get();
                        // IF latest version of this annotation sememe is inactive then reactivate it
                        if (! existingDescriptionExtendedTypeSemanticChronology.isLatestVersionActive(this.readCoordinate)) {
                           LatestVersion<DynamicVersion<?>> latestInactiveVersionOptional = 
                                 existingDescriptionExtendedTypeSemanticChronology.getLatestVersion(this.readCoordinate.makeCoordinateAnalog(Status.ANY_STATUS_SET));
                           // TODO handle contradictions
                           DynamicVersion<?> latestInactiveVersion = latestInactiveVersionOptional.get();
                           DynamicImpl newDescriptionActiveExtendedTypeSememeVersion = existingDescriptionExtendedTypeSemanticChronology
                                 .createMutableVersion(Status.ACTIVE, this.editCoordinate);
                           newDescriptionActiveExtendedTypeSememeVersion.setData(latestInactiveVersion.getData());
                           importUtil_.storeManualUpdate(existingDescriptionExtendedTypeSemanticChronology);
                        }
                     } else {
                        // Shouldn't happen
                     }
                  }

                  //not allowing them to set the value to empty, just assume they only meant to change status in the case where new value is missing
                  MutableDescriptionVersion mss = (MutableDescriptionVersion)sc
                     .createMutableVersion(d.isActive() ? Status.ACTIVE : Status.INACTIVE, this.editCoordinate);
                  mss.setText(StringUtils.isBlank(newValue) ? 
                        (StringUtils.isBlank(d.getValueOld()) ? latest.get().getText() : d.getValueOld()) 
                        : newValue);
                  mss.setCaseSignificanceConceptNid(latest.get().getCaseSignificanceConceptNid());
                  mss.setDescriptionTypeConceptNid(latest.get().getDescriptionTypeConceptNid());
                  mss.setLanguageConceptNid(latest.get().getLanguageConceptNid());

                  //No changing of type name, code, or vuid
               }
               else
               {
                  throw new RuntimeException("Unexpected sememe type!");
               }
               importUtil_.storeManualUpdate(sc);
            }
            else
            {
               //moveFromConceptCode is the VUID of the concept where this description currently exists.
               //if populated, we need to find this description under the old concept, and retire it, while copying all nested items here...
               
               UUID sourceConcept = findConcept(d.getMoveFromConceptCode()).get();
               //we tested this lookup in an earlier error checking pass above, it shouldn't come back null.
               UUID oldDescription = findDescription(sourceConcept, d.getCode()).orElseThrow(() -> new RuntimeException("oops"));
               
               SemanticChronology oldSc = Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(oldDescription));
               
               LatestVersion<DescriptionVersion> latest = oldSc.getLatestVersion(this.readCoordinate);
               if (!latest.isPresent())
               {
                  throw new RuntimeException("Unexected!");
               }
               
               //Make a new description with the provided and/or old values
               final SemanticChronology newDescription = 
                     importUtil_.addDescription(concept, createNewDescriptionUuid(concept.getPrimordialUuid(), d.getCode()), 
                     StringUtils.isBlank(newValue) ? 
                           (StringUtils.isBlank(d.getValueOld()) ? latest.get().getText() : d.getValueOld()) 
                           : newValue,
                     DescriptionType.parse(latest.get().getDescriptionTypeConceptNid()), 
                     Frills.isDescriptionPreferred(latest.get().getNid(), this.readCoordinate),
                     StringUtils.isBlank(d.getTypeName()) ? 
                           Frills.getDescriptionExtendedTypeConcept(this.readCoordinate, latest.get().getNid(), true).orElse(null)
                           : extendedDescriptionTypeNameMap.get(d.getTypeName().toLowerCase()), 
                     d.isActive() == null ? latest.get().getStatus() : (d.isActive() ? Status.ACTIVE : Status.INACTIVE));
               descRef = ComponentReference.fromChronology(newDescription);
               
               //copy all other nested components
               Get.assemblageService().getSemanticChronologyStreamForComponent(oldSc.getNid()).forEach(existingNestedSememe ->
               {
                  if (existingNestedSememe.getAssemblageNid() == DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getNid() ||
                        existingNestedSememe.getAssemblageNid() == MetaData.CODE____SOLOR.getNid() ||
                        existingNestedSememe.getAssemblageNid() == MetaData.VUID____SOLOR.getNid() ||
                        existingNestedSememe.getAssemblageNid() == MetaData.US_ENGLISH_DIALECT____SOLOR.getNid())
                  {
                     //ignore - these are handled with special case code above and below....
                  }
                  else
                  {
                     LatestVersion<SemanticVersion> latestVersionOfExistingNestedSememe = 
                        existingNestedSememe.getLatestVersion(this.readCoordinate);
                     
                     if (latestVersionOfExistingNestedSememe.isPresent() && latestVersionOfExistingNestedSememe.get().getStatus() == Status.ACTIVE)
                     {
                        SemanticChronology copyOfExistingNestedSememe = null;
                        
                        if (!latestVersionOfExistingNestedSememe.contradictions().isEmpty()) {
                           // TODO handle contradictions
                        }
                        //expect these to be, primarily, dynamic sememes, refset entries or strings...
                        switch (existingNestedSememe.getVersionType())
                        {
                        case DYNAMIC:
                           copyOfExistingNestedSememe = importUtil_.addAnnotation(
                                 ComponentReference.fromSememe(newDescription.getPrimordialUuid()),
                                 null,
                                 ((DynamicVersion<?>)latestVersionOfExistingNestedSememe.get()).getData(),
                                 Get.identifierService().getUuidPrimordialForNid(existingNestedSememe.getAssemblageNid()).get(),
                                 Status.ACTIVE,
                                 null,
                                 null);
                           break;
                        case MEMBER:
                           SemanticVersion memberSememe = (SemanticVersion)latestVersionOfExistingNestedSememe.get();
                           copyOfExistingNestedSememe = importUtil_.addAssemblageMembership(ComponentReference.fromSememe(newDescription.getPrimordialUuid()), 
                                 Get.identifierService().getUuidPrimordialForNid(memberSememe.getAssemblageNid()).get(), Status.ACTIVE, null);
                           break;
                        case STRING:
                           StringVersion stringSememe = (StringVersion)latestVersionOfExistingNestedSememe.get();
                           // TODO is Get.identifierService().getUuidPrimordialForNid(stringSememe.getAssemblageNid()) == refsetUuid?
                           copyOfExistingNestedSememe = importUtil_.addStringAnnotation(ComponentReference.fromSememe(newDescription.getPrimordialUuid()), 
                                 stringSememe.getString(), Get.identifierService().getUuidPrimordialForNid(stringSememe.getAssemblageNid()).get(), 
                                 Status.ACTIVE);
                           break;

                           //None of these are expected in vhat data
                        case DESCRIPTION:
                        case LOGIC_GRAPH:
                        case LONG:
                        case COMPONENT_NID:
                        case UNKNOWN:
                        default:
                           throw new RuntimeException("MoveFromConceptCode doesn't supported nested sememes of type " + existingNestedSememe.getVersionType() + 
                                 " for designation " + d.getCode());
                        }

                        if (copyOfExistingNestedSememe != null) {
                           copy(importUtil_, copyOfExistingNestedSememe, existingNestedSememe, this.readCoordinate, this.editCoordinate);
                        }
                     }
                  }
               });
               
               Long vuidToMigrate = d.getVUID() == null ? Frills.getVuId(latest.get().getNid(), this.readCoordinate).orElse(null) : d.getVUID();
               
               if (vuidToMigrate != null)
               {
                  importUtil_.addStaticStringAnnotation(descRef, vuidToMigrate.toString(), MetaData.VUID____SOLOR.getPrimordialUuid(), Status.ACTIVE);
               }
               
               String codeToMigrate = null;
               if (StringUtils.isBlank(d.getCode()))
               {
                  List<String> oldCodes = Frills.getCodes(latest.get().getNid(), this.readCoordinate);
                  if (oldCodes.size() > 0)
                  {
                     codeToMigrate = oldCodes.get(0);
                     if (oldCodes.size() > 1)
                     {
                        LOG.warn("More than one code on concept " + concept.getPrimordialUuid());
                     }
                  }
                  else if (vuidToMigrate != null)
                  {
                     codeToMigrate = vuidToMigrate.toString();
                  }
               }
               else
               {
                  //use the new code
                  codeToMigrate = d.getCode();
               }
               if (StringUtils.isNotBlank(codeToMigrate))
               {
                  importUtil_.addStaticStringAnnotation(descRef, codeToMigrate, MetaData.CODE____SOLOR.getPrimordialUuid(), Status.ACTIVE);
               }
               
               
               //retire the old sememe:
               try
               {
                  Optional<Chronology> oc = Frills.resetStatusWithNoCommit(Status.INACTIVE, oldSc.getNid(), this.editCoordinate, this.readCoordinate);
                  if (oc.isPresent())
                  {
                     importUtil_.storeManualUpdate(oc.get());
                  }
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
               
               //retire nested components
               for (Chronology o : recursiveRetireNested(oldSc.getPrimordialUuid()))
               {
                  importUtil_.storeManualUpdate(o);
               }
            }
            
            break;
         default :
            throw new RuntimeException("Unexpected error");
         
      }
      
      if (descRef == null)
      {
         descRef = ComponentReference.fromSememe(findDescription(concept.getPrimordialUuid(), d.getCode()).get());
      }
      
      if (d instanceof Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation)
      {
         Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation dd = 
               (Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation)d;
         loadDesignationProperties(descRef, dd.getProperties());
         loadSubsetMembership(descRef, dd.getSubsetMemberships());
      }
   }
   
   private static void copy(
         IBDFCreationUtility importUtil,
         SemanticChronology existingParentComponent,
         SemanticChronology copyOfParentComponent,
         StampCoordinate readCoordinate, EditCoordinate editCoordinate) {
      Get.assemblageService().getSemanticChronologyStreamForComponent(existingParentComponent.getNid()).forEach(existingNestedSememe -> {
         LatestVersion<Version> latestVersionOfExistingNestedSememe = 
            existingNestedSememe.getLatestVersion(readCoordinate);

         if (latestVersionOfExistingNestedSememe.isPresent() && latestVersionOfExistingNestedSememe.get().getStatus() == Status.ACTIVE)
         {
            if (!latestVersionOfExistingNestedSememe.contradictions().isEmpty()) {
               // TODO handle contradictions
            }
            
            SemanticChronology copyOfExistingNestedSememe = null;

            //expect these to be, primarily, dynamic sememes, refset entries or strings...
            switch (latestVersionOfExistingNestedSememe.get().getChronology().getVersionType())
            {
            case DYNAMIC:
               DynamicVersion<?> dynamicSememe = (DynamicVersion<?>)latestVersionOfExistingNestedSememe.get();
               copyOfExistingNestedSememe = importUtil.addAnnotation(
                     ComponentReference.fromChronology(copyOfParentComponent),
                     null,
                     dynamicSememe.getData(),
                     Get.identifierService().getUuidPrimordialForNid(existingNestedSememe.getAssemblageNid()).get(),
                     Status.ACTIVE,
                     null,
                     null);
               break;
            case MEMBER:
               SemanticVersion memberSememe = (SemanticVersion)latestVersionOfExistingNestedSememe.get();
               copyOfExistingNestedSememe = importUtil.addAssemblageMembership(ComponentReference.fromChronology(copyOfParentComponent), Get.identifierService()
                     .getUuidPrimordialForNid(memberSememe.getAssemblageNid()).get(), Status.ACTIVE, null);
               
               break;
            case STRING:
               StringVersion stringSememe = (StringVersion)latestVersionOfExistingNestedSememe.get();
               // TODO is Get.identifierService().getUuidPrimordialForNid(stringSememe.getAssemblageNid()) == refsetUuid?
               copyOfExistingNestedSememe = importUtil.addStringAnnotation(ComponentReference.fromChronology(copyOfParentComponent), stringSememe.getString(), 
                     Get.identifierService().getUuidPrimordialForNid(stringSememe.getAssemblageNid()).get(), Status.ACTIVE);
               break;

               //None of these are expected in vhat data
            case DESCRIPTION:
            case LOGIC_GRAPH:
            case LONG:
            case COMPONENT_NID:
            case UNKNOWN:
            default:
               throw new RuntimeException("MoveFromConceptCode doesn't supported nested sememes of type " + existingParentComponent.getVersionType());
            }

            if (copyOfExistingNestedSememe != null) {
               copy(importUtil, copyOfExistingNestedSememe, existingNestedSememe, readCoordinate, editCoordinate);
            }
         }
      });
   }
   /**
    * Retire any sememes attached to this component.  Do not change the component.
    * @param component
    */
   private List <Chronology> recursiveRetireNested(UUID component) 
   {
      ArrayList<Chronology> updated = new ArrayList<>();
      Get.assemblageService().getSemanticChronologyStreamForComponent(Get.identifierService().getNidForUuids(component)).forEach(sememe ->
      {
         try 
         {
            Optional<Chronology> oc = Frills.resetStatusWithNoCommit(Status.INACTIVE, sememe.getNid(), this.editCoordinate, this.readCoordinate);
            if (oc.isPresent())
            {
               updated.add(oc.get());
            }
            updated.addAll(recursiveRetireNested(sememe.getPrimordialUuid()));
         } catch (Exception e) 
         {
            throw new RuntimeException(e);
         }
      });
      return updated;
   }

   /**
    * @param primordialUuid
    * @param code
    * @return
    */
   private Optional<UUID> findDescription(UUID concept, String descriptionCode)
   {
      if (concept == null)
      {
         return Optional.empty();
      }
      return Get.assemblageService().getDescriptionsForComponent(Get.identifierService().getNidForUuids(concept)).stream().filter(desc ->
      {
         return findPropertySememe(desc.getPrimordialUuid(),  MetaData.CODE____SOLOR.getPrimordialUuid(), descriptionCode).isPresent();
      }).findAny().<UUID>map(desc -> desc.getPrimordialUuid());
   }
   
   private Optional<UUID> findConcept(String conceptCode)
   {
      IndexQueryService si = LookupService.get().getService(IndexQueryService.class, "sememe indexer");
      if (si != null) {
         //force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
         ArrayList<SemanticChronology> candidates = new ArrayList<>();
         List<SearchResult> result = si.query(conceptCode + " ", true, new Integer[] {MetaData.CODE____SOLOR.getNid()}, null, null, 50, Long.MAX_VALUE);
         result.forEach(sr -> 
         {
            SemanticChronology sc = Get.assemblageService().getSemanticChronology(sr.getNid());
            LatestVersion<StringVersion> ss = sc.getLatestVersion(this.readCoordinate);
            if (ss.isPresent() && ss.get().getStatus() == Status.ACTIVE && ss.get().getString().equals(conceptCode))
            {
               candidates.add(sc);
            }
         });
         if (candidates.size() == 0)
         {
            return Optional.empty();
         }
         else if (candidates.size() > 1)
         {
            throw new RuntimeException("There is more than one concept in the system with a 'Code' of '" + conceptCode + "'");
         }
         else
         {
            return Get.identifierService().getUuidPrimordialForNid(candidates.get(0).getReferencedComponentNid());
         }
      } else {
         LOG.warn("Sememe Index not available - can't lookup VUID");
      }
      return Optional.empty();
   }

   /**
    * @param subsetMemberships
    */
   private void loadSubsetMembership(ComponentReference description, SubsetMemberships subsetMemberships)
   {
      if (subsetMemberships != null)
      {
         for(SubsetMembership sm : subsetMemberships.getSubsetMembership())
         {
            switch (sm.getAction())
            {
               case ADD:
                  //subset lookups validated previously
                  importUtil_.addAssemblageMembership(description, this.vuidToSubsetMap.get(sm.getVUID()), sm.isActive() ? Status.ACTIVE : Status.INACTIVE, null);
                  break;
               case NONE:
                  //noop
                  break;
               case REMOVE:
                  // REMOVE directive takes precedence. Explicitely set active=false, and fall-through
                  sm.setActive(false);
               case UPDATE:
                  Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(description.getNid(), Get.identifierService()
                     //There really shouldn't be more than one of these, but if there is, no harm in changing state on all of them.
                     .getNidForUuids(this.vuidToSubsetMap.get(sm.getVUID()))).forEach(sc -> 
                     {
                        LatestVersion<DynamicVersion<?>> ds = sc.getLatestVersion(this.readCoordinate);
                        if (ds.isPresent())
                        {
                           sc.createMutableVersion(sm.isActive() ? Status.ACTIVE : Status.INACTIVE, this.editCoordinate);
                           importUtil_.storeManualUpdate(sc);
                        }
                     });
                  break;
               default :
                  throw new RuntimeException("Unexpected Error");
               
            }
         }
      }
   }

   /**
    * @param properties
    */
   private void loadDesignationProperties(ComponentReference designationSememe, Properties properties)
   {
      if (properties != null)
      {
         for (PropertyType p : properties.getProperty())
         {
            handleProperty(designationSememe, p.getTypeName(),  p.getValueOld(),  p.getValueNew(),  p.isActive(),  p.getAction());
         }
      }
   }
   
   private void loadRelationships(ComponentReference concept, Relationships relationships)
   {
      if (relationships != null)
      {
         HashSet<UUID> gatheredisA = new HashSet<>();
         HashSet<UUID> retireIsA = new HashSet<>();
         LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
         for (Relationship r : relationships.getRelationship())
         {
            Optional<UUID> newTarget = StringUtils.isBlank(r.getNewTargetCode()) ? Optional.empty() : findConcept(StringUtils.trim(r.getNewTargetCode()));
            
            switch(r.getAction())
            {
               case ADD:
                  importUtil_.addAssociation(concept, null, newTarget.get(), this.associations.getProperty(StringUtils.trim(r.getTypeName())).getUUID(), 
                     r.isActive() ? Status.ACTIVE : Status.INACTIVE, null, null);
                  if ("has_parent".equals(StringUtils.trim(r.getTypeName())) && r.isActive())
                  {
                     gatheredisA.add(newTarget.get());
                  }
                  break;
               case NONE:
                  //noop
                  break;
               case REMOVE:
                  // REMOVE directive takes precedence. Explicitly set active=false, and fall-through
                  r.setActive(false);
               case UPDATE:
                  Optional<UUID> oldTarget = findConcept(r.getOldTargetCode());
                  UUID existingAssociation = findAssociationSememe(concept.getPrimordialUuid(), this.associations.getProperty(StringUtils.trim(r.getTypeName())).getUUID(), 
                     oldTarget.get()).get();
                  
                  SemanticChronology sc = Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(existingAssociation));
                  
                  LatestVersion<Version> ds = sc.getLatestVersion(this.readCoordinate);
                  if (ds.isPresent())
                  {
                     MutableDynamicVersion<?> mds = sc.createMutableVersion(
                           r.isActive() ? Status.ACTIVE : Status.INACTIVE, this.editCoordinate);
                     mds.setData(new DynamicData[] {new DynamicUUIDImpl(newTarget.isPresent() ? newTarget.get() : oldTarget.get())});
                     importUtil_.storeManualUpdate(sc);
                  }
                  else
                  {
                     throw new RuntimeException("Couldn't find existing association for " + StringUtils.trim(r.getTypeName()) + " " + r.getNewTargetCode());
                  }
                  
                  if ("has_parent".equals(StringUtils.trim(r.getTypeName())))
                  {
                     if (r.isActive())
                     {
                        UUID xTarget = newTarget.isPresent() ? newTarget.get() : oldTarget.get();
                        gatheredisA.add(xTarget);
                     }
                     else
                     {
                        retireIsA.add(oldTarget.get());
                        if (newTarget.isPresent())
                        {
                           retireIsA.add(newTarget.get());
                        }
                     }
                  }
                  
                  break;
               default :
                  throw new RuntimeException("Unexpected error");
            }
         }
         
         Optional<SemanticChronology> logicGraph = Frills.getStatedDefinitionChronology(concept.getNid(), this.logicReadCoordinate);
         
         LogicalExpression le = null;         
         if (logicGraph.isPresent())
         {
            ArrayList<ConceptAssertion> cas = new ArrayList<>(gatheredisA.size());
            
            if (gatheredisA.size() > 0)
            {
               for (UUID uuid : gatheredisA)
               {
                  cas.add(ConceptAssertion(Get.identifierService().getNidForUuids(uuid), leb));
               }
            }
            
            for (int parent : Get.taxonomyService().getSnapshot(new ManifoldCoordinateImpl(this.readCoordinate, 
                  LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate()))
               .getTaxonomyParentConceptNids(concept.getNid()))
               {
                  UUID potential = Get.identifierService().getUuidPrimordialForNid(parent).get();
                  
                  if (!gatheredisA.contains(potential) && !retireIsA.contains(potential))
                  {
                     //Nothing was said about this pre-existing parent, so keep it.
                     cas.add(ConceptAssertion(Get.identifierService().getNidForUuids(potential), leb));
                  }
               };
            
            if (cas.size() > 0)
            {
               NecessarySet(And(cas.toArray(new Assertion[gatheredisA.size()])));
               
               le = leb.build();
               
               MutableLogicGraphVersion mlgs = logicGraph.get().createMutableVersion(Status.ACTIVE, this.editCoordinate);
               
               mlgs.setGraphData(le.getData(DataTarget.INTERNAL));
            }
            else
            {
               //If we ended up with nothing active, just read the current, and set the entire thing to inactive.
               MutableLogicGraphVersion mlgs = logicGraph.get().createMutableVersion(Status.INACTIVE, this.editCoordinate);
                  mlgs.setGraphData(Frills.getLogicGraphVersion(logicGraph.get(), this.readCoordinate).get().getGraphData());
            }
            
            importUtil_.storeManualUpdate(logicGraph.get());
         }
         else
         {
            if (gatheredisA.size() > 0)
            {
               ArrayList<ConceptAssertion> cas = new ArrayList<>(gatheredisA.size());
               
               for (UUID uuid : gatheredisA)
               {
                  cas.add(ConceptAssertion(Get.identifierService().getNidForUuids(uuid), leb));
               }
               
               NecessarySet(And(cas.toArray(new Assertion[gatheredisA.size()])));
               le = leb.build();
               importUtil_.addRelationshipGraph(concept, null, le, true, null,  null, VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE.getPrimordialUuid());
            }
         }
      }   
   }
   
   private void loadMapSets(MapSets mapsets) throws IOException {
      
      if (mapsets != null)
      {
         for (MapSet ms : mapsets.getMapSet())
         {
            //before defining the columns, we need to determine if this mapset makes use of gem flags
            boolean mapSetDefinitionHasGemFlag = false;
            if (ms.getMapEntries() != null && ms.getMapEntries().getMapEntry() != null)
            for (MapEntry me : ms.getMapEntries().getMapEntry())
            {
               if (mapSetDefinitionHasGemFlag)
               {
                  break;
               }
               if (me.getProperties() != null && me.getProperties().getProperty() != null)
               {
                  for (Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties.Property mapItemProperty :
                     me.getProperties().getProperty())
                  {
                     if (mapItemProperty.getTypeName().equals("GEM_Flags"))
                     {
                        mapSetDefinitionHasGemFlag = true;
                        break;
                     }
                  }
               }
            }
            
            String mapSetVuid;
            
            ComponentReference concept = null;
            switch (ms.getAction())
            {
               case ADD:
                  mapSetVuid = ms.getVUID() == null ? 
                        (this.vuidSupplier == null ? null : this.vuidSupplier.getAsLong() + "")
                        : ms.getVUID().toString();
                  
                  String code = StringUtils.isBlank(ms.getCode()) ? mapSetVuid : ms.getCode();
                  
                  if (StringUtils.isBlank(code))
                  {
                     throw new RuntimeException("No code supplied, and vuid generation is disabled!");
                  }
                  concept = ComponentReference.fromConcept(importUtil_.createConcept(createNewConceptUuid(code), null, 
                              ms.isActive() ? Status.ACTIVE : Status.INACTIVE, null));
                        
                  if (StringUtils.isNotBlank(mapSetVuid))
                  {
                     importUtil_.addStaticStringAnnotation(concept, mapSetVuid, MetaData.VUID____SOLOR.getPrimordialUuid(), Status.ACTIVE);
                  }
                  
                  if (StringUtils.isNotBlank(code))
                  {
                     importUtil_.addStaticStringAnnotation(concept, code, MetaData.CODE____SOLOR.getPrimordialUuid(), Status.ACTIVE);
                  }
                  
                  importUtil_.addAssociation(concept, null, this.subsets.getPropertyTypeUUID(), this.associations.getProperty("has_parent").getUUID(), 
                        Status.ACTIVE, null, null);
                  importUtil_.addAssociation(concept, null, this.subsets.getAltMetaDataParentUUID(), this.associations.getProperty("has_parent").getUUID(), 
                        Status.ACTIVE, null, null);
                  importUtil_.addAssociation(concept, null, IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMEME_TYPE.getPrimordialUuid(), 
                        this.associations.getProperty("has_parent").getUUID(), Status.ACTIVE, null, null);
                  
                  LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
                  
                  NecessarySet(And(new Assertion[] {
                        ConceptAssertion(Get.identifierService().getNidForUuids(this.subsets.getAltMetaDataParentUUID()), leb),
                        ConceptAssertion(Get.identifierService().getNidForUuids(this.subsets.getPropertyTypeUUID()), leb),
                        ConceptAssertion(Get.identifierService().getNidForUuids(
                              IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMEME_TYPE.getPrimordialUuid()), leb)}));

                  LogicalExpression le = leb.build();
                  
                  importUtil_.addRelationshipGraph(concept, null, le, true, null,  null);
                  
                  DynamicColumnInfo[] columns = new DynamicColumnInfo[mapSetDefinitionHasGemFlag ? 6 : 5];
                  int col = 0;
                  columns[col] = new DynamicColumnInfo(col++, DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(),
                        DynamicDataType.UUID, null, false, DynamicValidatorType.COMPONENT_TYPE,
                        new DynamicArrayImpl<>(new DynamicString[] { new DynamicStringImpl(ObjectChronologyType.CONCEPT.name()) }), true);
                  columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EQUIVALENCE_TYPE.getUUID(), DynamicDataType.UUID,
                        null, false, DynamicValidatorType.IS_KIND_OF, new DynamicUUIDImpl(IsaacMappingConstants.get().MAPPING_EQUIVALENCE_TYPES.getUUID()), true);
                  columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_SEQUENCE.getUUID(), DynamicDataType.INTEGER,
                        null, false, true);
                  columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GROUPING.getUUID(), DynamicDataType.LONG,
                        null, false, true);
                  columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_EFFECTIVE_DATE.getUUID(),
                        DynamicDataType.LONG, null, false, true);
                  //moved to end - make it more convenient for GUI where target and qualifier are extracted, and used elsewhere - its convenient not to have the order change.
                  if (mapSetDefinitionHasGemFlag)
                  {
                     columns[col] = new DynamicColumnInfo(col++, IsaacMappingConstants.get().DYNAMIC_COLUMN_MAPPING_GEM_FLAGS.getUUID(),
                           DynamicDataType.STRING, null, false, true);
                  }
                  
                  importUtil_.configureConceptAsDynamicRefex(concept, ms.getName(), columns, ObjectChronologyType.CONCEPT, null);
                  
                  //Annotate this concept as a mapset definition concept.
                  importUtil_.addAnnotation(concept, null, null, IsaacMappingConstants.get().DYNAMIC_SEMANTIC_MAPPING_SEMEME_TYPE.getUUID(), Status.ACTIVE, null);
                  
                  //Now that we have defined the map sememe, add the other annotations onto the map set definition.
                  if (StringUtils.isNotBlank(ms.getSourceCodeSystem()))
                  {
                     importUtil_.addAnnotation(concept, null, 
                        new DynamicData[] {
                              new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM.getNid()),
                              new DynamicStringImpl(ms.getSourceCodeSystem())},
                        IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
                  }
                  
                  if (StringUtils.isNotBlank(ms.getSourceVersionName()))
                  {
                     importUtil_.addAnnotation(concept, null, 
                        new DynamicData[] {
                              new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_SOURCE_CODE_SYSTEM_VERSION.getNid()),
                              new DynamicStringImpl(ms.getSourceVersionName())},
                        IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
                  }
                  
                  if (StringUtils.isNotBlank(ms.getTargetCodeSystem()))
                  {
                     importUtil_.addAnnotation(concept, null, 
                        new DynamicData[] {
                              new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM.getNid()),
                              new DynamicStringImpl(ms.getTargetCodeSystem())},
                        IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
                  }
                  
                  if (StringUtils.isNotBlank(ms.getTargetVersionName()))
                  {
                     importUtil_.addAnnotation(concept, null, 
                        new DynamicData[] {
                              new DynamicNidImpl(IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM_VERSION.getNid()),
                              new DynamicStringImpl(ms.getTargetVersionName())},
                        IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getPrimordialUuid(), Status.ACTIVE, null, null);
                  }
                  break;
               case NONE:
                  //noop
                  break;
               case REMOVE:
                  concept = ComponentReference.fromConcept(importUtil_.createConcept(findConcept(ms.getCode()).get(), null, Status.INACTIVE, null));
                  for (Chronology o : recursiveRetireNested(concept.getPrimordialUuid()))
                  {
                     if (o != null) {
                        importUtil_.storeManualUpdate(o);
                     }
                  }
                  break;
               case UPDATE:
                  //We could, potentially support updating vuid, but the current system doesn't.
                  //Also, source / target stuff, but leaving as unhandled, for now.
                  //so we only process activate / inactivate changes here.
                  concept = ComponentReference.fromConcept(importUtil_.createConcept(findConcept(ms.getCode()).get(), null, 
                     ms.isActive() ? Status.ACTIVE : Status.INACTIVE, null));
                  break;
               default :
                  throw new RuntimeException("Unexpected error");
            }
            if (concept == null)
            {
               concept = ComponentReference.fromConcept(findConcept(ms.getCode()).get());
            }
            loadDesignations(concept, ms.getDesignations());
            loadConceptProperties(concept, ms.getProperties());
            
            for (MapEntry me : ms.getMapEntries().getMapEntry())
            {
               if (me.getAction() == ActionType.ADD || me.getAction() == ActionType.UPDATE)
               {
                  String gemFlag = null;
                  if (me.getProperties() != null && me.getProperties().getProperty() != null)
                  {
                     for (Terminology.CodeSystem.Version.MapSets.MapSet.MapEntries.MapEntry.Properties.Property property : me.getProperties().getProperty())
                     {
                        if (property.getTypeName().equals("GEM_Flags"))
                        {
                           gemFlag = property.getValueNew();
                           break;
                        }
                     }
                  }
                  
                  DynamicData[] columnData = new DynamicData[mapSetDefinitionHasGemFlag ? 6 : 5];
                  int col = 0;
                  columnData[col++] = new DynamicUUIDImpl(findConcept(me.getTargetCode()).get());
                  columnData[col++] = null;  //qualifier column
                  columnData[col++] = new DynamicIntegerImpl(me.getSequence()); //sequence column
                  columnData[col++] = me.getGrouping() != null ? new DynamicLongImpl(me.getGrouping()) : null; //grouping column
                  columnData[col++] = me.getEffectiveDate() != null ? new DynamicLongImpl(me.getEffectiveDate().toGregorianCalendar()
                        .getTimeInMillis()) : null; //effectiveDate
                  if (mapSetDefinitionHasGemFlag )
                  {
                     columnData[col++] = gemFlag == null ? null : new DynamicStringImpl(gemFlag);
                  }
                  
                  if (me.getAction() == ActionType.ADD)
                  {
                     String mapEntryVuid = me.getVUID() == null ? this.vuidSupplier.getAsLong() + "" : me.getVUID().toString();
                     
                     SemanticChronology association = importUtil_.addAnnotation(ComponentReference.fromConcept(findConcept(me.getSourceCode()).get()), 
                           createNewMapItemUUID(concept.getPrimordialUuid(),mapEntryVuid), columnData, concept.getPrimordialUuid(), 
                           me.isActive() ? Status.ACTIVE : Status.INACTIVE, null, null);
                     
                     importUtil_.addStaticStringAnnotation(ComponentReference.fromChronology(association, () -> "Association"), 
                           mapEntryVuid, this.annotations.getProperty("VUID").getUUID(), Status.ACTIVE);
                  }
                  else
                  {
                     SemanticChronology sc = Get.assemblageService().getSemanticChronology(Get.identifierService().getNidForUuids(
                           createNewMapItemUUID(concept.getPrimordialUuid(), me.getVUID().toString())));
                     
                     MutableDynamicVersion<?> mds = sc.createMutableVersion(me.isActive() ? Status.ACTIVE : Status.INACTIVE, this.editCoordinate);
                     mds.setData(columnData);
                     importUtil_.storeManualUpdate(sc);

                  }
               }
               if (me.getAction() == ActionType.REMOVE)
               {
                  try
                  {
                     Optional<Chronology> oc = Frills.resetStatusWithNoCommit(Status.INACTIVE, Get.identifierService().getNidForUuids(
                           createNewMapItemUUID(concept.getPrimordialUuid(), me.getVUID().toString())), this.editCoordinate, this.readCoordinate);
                     if (oc.isPresent())
                     {
                        importUtil_.storeManualUpdate(oc.get());
                     }
                  }
                  catch (Exception e)
                  {
                     throw new IOException(e);
                  }
               }
            }
         }
      }
   }
}

