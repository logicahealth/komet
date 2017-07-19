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



package sh.isaac.provider.ibdf.diff;

//~--- JDK imports ------------------------------------------------------------

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.component.sememe.SememeBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.relationship.RelationshipVersionAdaptor;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.relationship.RelationshipAdaptorChronicleKeyImpl;
import sh.isaac.model.relationship.RelationshipVersionAdaptorImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeLongImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeNidImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeStringImpl;
import sh.isaac.model.sememe.version.ComponentNidSememeImpl;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;
import sh.isaac.model.sememe.version.DynamicSememeImpl;
import sh.isaac.model.sememe.version.LogicGraphSememeImpl;
import sh.isaac.model.sememe.version.LongSememeImpl;
import sh.isaac.model.sememe.version.SememeVersionImpl;
import sh.isaac.model.sememe.version.StringSememeImpl;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.MutableComponentNidVersion;
import sh.isaac.api.component.sememe.version.MutableDescriptionVersion;
import sh.isaac.api.component.sememe.version.MutableDynamicVersion;
import sh.isaac.api.component.sememe.version.MutableLogicGraphVersion;
import sh.isaac.api.component.sememe.version.MutableLongVersion;
import sh.isaac.api.component.sememe.version.MutableStringVersion;
import sh.isaac.api.component.sememe.version.StringVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Utility methods in support of BinaryDataDifferProvider used to see if two
 * components are the same and to create new versions when necessary.
 *
 * {@link BinaryDataDifferProvider}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class BinaryDataDifferProviderUtility {
   /** The component change found. */
   static boolean componentChangeFound = false;

   /** The new import date. */
   static long newImportDate;

   //~--- fields --------------------------------------------------------------

   /** The diff on status. */
   boolean diffOnStatus;

   /** The diff on timestamp. */
   boolean diffOnTimestamp;

   /** The diff on author. */
   boolean diffOnAuthor;

   /** The diff on module. */
   boolean diffOnModule;

   /** The diff on path. */
   boolean diffOnPath;

   /** The sememe builder service. */
   private final SememeBuilderService<?> sememeBuilderService;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new binary data differ provider utility.
    *
    * @param diffOnStatus the diff on status
    * @param diffOnTimestamp the diff on timestamp
    * @param diffOnAuthor the diff on author
    * @param diffOnModule the diff on module
    * @param diffOnPath the diff on path
    */
   public BinaryDataDifferProviderUtility(Boolean diffOnStatus,
         Boolean diffOnTimestamp,
         Boolean diffOnAuthor,
         Boolean diffOnModule,
         Boolean diffOnPath) {
      this.diffOnStatus          = diffOnStatus;
      this.diffOnTimestamp       = diffOnTimestamp;
      this.diffOnAuthor          = diffOnAuthor;
      this.diffOnModule          = diffOnModule;
      this.diffOnPath            = diffOnPath;
      this.sememeBuilderService = Get.sememeBuilderService();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the new inactive version.
    *
    * @param oldChron the old chron
    * @param type the type
    * @param inactiveStampSeq the inactive stamp seq
    * @return the ochre externalizable
    */
   public OchreExternalizable addNewInactiveVersion(OchreExternalizable oldChron,
         OchreExternalizableObjectType type,
         int inactiveStampSeq) {
      final LatestVersion<StampedVersion> latestVersion =
         ((Chronology<StampedVersion>) oldChron).getLatestVersion(StampedVersion.class,
                                                                        StampCoordinates.getDevelopmentLatestActiveOnly());

      if (type == OchreExternalizableObjectType.CONCEPT) {
         ((ConceptVersion) latestVersion.value().get()).getChronology()
               .createMutableVersion(inactiveStampSeq);
      } else if (type == OchreExternalizableObjectType.SEMEME) {
         final SememeVersion originalVersion = (SememeVersion) latestVersion.value().get();
         SememeVersionImpl createdVersion = (SememeVersionImpl)
                 ((SememeChronologyImpl) originalVersion.getChronology())
                                                       .createMutableVersion(
                                                          getSememeClass((SememeVersion) latestVersion.value().get()),
                                                                inactiveStampSeq);

         populateData(createdVersion, originalVersion, inactiveStampSeq);
      }

      return oldChron;
   }

   /**
    * Diff.
    *
    * @param oldChron the old chron
    * @param newChron the new chron
    * @param stampSeq the stamp seq
    * @param type the type
    * @return the ochre externalizable
    */
   public OchreExternalizable diff(Chronology<?> oldChron,
                                   Chronology<?> newChron,
                                   int stampSeq,
                                   OchreExternalizableObjectType type) {
      List<StampedVersion>       oldVersions = null;
      final List<StampedVersion> newVersions = (List<StampedVersion>) newChron.getVersionList();

      if (oldChron == null) {
         return createNewChronology(newChron, type, stampSeq);
      }

      boolean newVersionAdded = false;

      oldVersions = (List<StampedVersion>) oldChron.getVersionList();

      for (final StampedVersion nv: newVersions) {
         boolean equivalenceFound = false;

         for (final StampedVersion ov: oldVersions) {
            if (isEquivalent(ov, nv, type)) {
               equivalenceFound = true;
               break;
            }
         }

         if (!equivalenceFound) {
            // versionsToAdd.add(ov);
            addNewActiveVersion(oldChron, nv, type, stampSeq);
            newVersionAdded = true;
         }
      }

      if (!newVersionAdded) {
         return null;
      }

      return oldChron;
   }

   /**
    * Adds the new active version.
    *
    * @param oldChron the old chron
    * @param newVersion the new version
    * @param type the type
    * @param activeStampSeq the active stamp seq
    */
   private void addNewActiveVersion(Chronology<?> oldChron,
                                    StampedVersion newVersion,
                                    OchreExternalizableObjectType type,
                                    int activeStampSeq) {
      try {
         if (type == OchreExternalizableObjectType.CONCEPT) {
            ((ConceptChronology) oldChron).createMutableVersion(((ConceptVersion) newVersion).getStampSequence());
         } else if (type == OchreExternalizableObjectType.SEMEME) {
            SememeVersion createdVersion =
               ((SememeChronology) oldChron).createMutableVersion(((SememeChronology<?>) oldChron).getClass(),
                                                                  ((SememeVersion) newVersion).getStampSequence());

            createdVersion = populateData(createdVersion, (SememeVersion) newVersion, activeStampSeq);
         }
      } catch (final Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Creates the new chronology.
    *
    * @param newChron the new chron
    * @param type the type
    * @param stampSeq the stamp seq
    * @return the ochre externalizable
    */
   private OchreExternalizable createNewChronology(Chronology<?> newChron,
         OchreExternalizableObjectType type,
         int stampSeq) {
      try {
         if (type == OchreExternalizableObjectType.CONCEPT) {
            return newChron;
         } else if (type == OchreExternalizableObjectType.SEMEME) {
            final List<Chronology<? extends StampedVersion>> builtObjects = new ArrayList<>();
            SememeChronology<?>                                    sememe       = null;

            for (final StampedVersion version: newChron.getVersionList()) {
               final SememeBuilder<?> builder = getBuilder((SememeVersion) version);

               sememe = builder.build(stampSeq, builtObjects);
            }

            return sememe;
         } else {
            throw new Exception("Unsupported OchreExternalizableObjectType: " + type);
         }
      } catch (final Exception e) {
         e.printStackTrace();
      }

      return null;
   }

   /**
    * Populate data.
    *
    * @param newVer the new ver
    * @param originalVersion the original version
    * @param inactiveStampSeq the inactive stamp seq
    * @return the sememe version
    */
   private SememeVersion populateData(SememeVersion newVer,
         SememeVersion originalVersion,
         int inactiveStampSeq) {
      switch (newVer.getChronology()
                    .getSememeType()) {
      case MEMBER:
         return newVer;

      case COMPONENT_NID:
         ((MutableComponentNidVersion<?>) newVer).setComponentNid(((ComponentNidVersion<?>) originalVersion).getComponentNid());
         return newVer;

      case DESCRIPTION:
         ((MutableDescriptionVersion) newVer).setText(((DescriptionVersion) originalVersion).getText());
         ((MutableDescriptionVersion) newVer).setDescriptionTypeConceptSequence(((DescriptionVersion) originalVersion).getDescriptionTypeConceptSequence());
         ((MutableDescriptionVersion) newVer).setCaseSignificanceConceptSequence(((DescriptionVersion) originalVersion).getCaseSignificanceConceptSequence());
         ((MutableDescriptionVersion) newVer).setLanguageConceptSequence(((DescriptionVersion) originalVersion).getLanguageConceptSequence());
         return newVer;

      case DYNAMIC:
         ((MutableDynamicVersion<?>) newVer).setData(((DynamicSememe<?>) originalVersion).getData());
         return newVer;

      case LONG:
         ((MutableLongVersion<?>) newVer).setLongValue(((LongVersion<?>) originalVersion).getLongValue());
         return newVer;

      case STRING:
         ((MutableStringVersion) newVer).setString(((StringVersion) originalVersion).getString());
         return newVer;

      case RELATIONSHIP_ADAPTOR:
         final RelationshipVersionAdaptorImpl origRelVer = (RelationshipVersionAdaptorImpl) originalVersion;
         final RelationshipAdaptorChronicleKeyImpl key =
            new RelationshipAdaptorChronicleKeyImpl(origRelVer.getOriginSequence(),
                                                    origRelVer.getDestinationSequence(),
                                                    origRelVer.getTypeSequence(),
                                                    origRelVer.getGroup(),
                                                    origRelVer.getPremiseType(),
                                                    origRelVer.getNodeSequence());

         return new RelationshipVersionAdaptorImpl(key, inactiveStampSeq);

      case LOGIC_GRAPH:
         ((MutableLogicGraphVersion) newVer).setGraphData(((LogicGraphVersion) originalVersion).getGraphData());
         return newVer;

      case UNKNOWN:
         throw new UnsupportedOperationException();
      }

      return null;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the builder.
    *
    * @param version the version
    * @return the builder
    */
   private SememeBuilder<?> getBuilder(SememeVersion version) {
      SememeBuilder<?> builder = null;

      switch (version.getChronology()
                     .getSememeType()) {
      case COMPONENT_NID:
         final ComponentNidVersion<?> compNidSememe = (ComponentNidVersion<?>) version;

         builder = this.sememeBuilderService.getComponentSememeBuilder(compNidSememe.getComponentNid(),
               compNidSememe.getReferencedComponentNid(),
               compNidSememe.getAssemblageSequence());
         break;

      case DESCRIPTION:
         final DescriptionVersion descSememe = (DescriptionVersion) version;

         builder =
            this.sememeBuilderService.getDescriptionSememeBuilder(descSememe.getCaseSignificanceConceptSequence(),
                  descSememe.getLanguageConceptSequence(),
                  descSememe.getDescriptionTypeConceptSequence(),
                  descSememe.getText(),
                  descSememe.getReferencedComponentNid());
         break;

      case DYNAMIC:
         final DynamicSememe<?> dynSememe = (DynamicSememe<?>) version;

         builder = this.sememeBuilderService.getDynamicSememeBuilder(dynSememe.getReferencedComponentNid(),
               dynSememe.getAssemblageSequence(),
               dynSememe.getData());
         break;

      case LONG:
         final LongVersion<?> longSememe = (LongVersion<?>) version;

         builder = this.sememeBuilderService.getLongSememeBuilder(longSememe.getLongValue(),
               longSememe.getReferencedComponentNid(),
               longSememe.getAssemblageSequence());
         break;

      case MEMBER:
         builder = this.sememeBuilderService.getMembershipSememeBuilder(version.getReferencedComponentNid(),
               version.getAssemblageSequence());
         break;

      case STRING:
         final StringVersion stringSememe = (StringVersion) version;

         builder = this.sememeBuilderService.getStringSememeBuilder(stringSememe.getString(),
               stringSememe.getReferencedComponentNid(),
               stringSememe.getAssemblageSequence());
         break;

      case LOGIC_GRAPH:
         final LogicGraphVersion logicGraphSememe = (LogicGraphVersion) version;

         builder = this.sememeBuilderService.getLogicalExpressionSememeBuilder(logicGraphSememe.getLogicalExpression(),
               logicGraphSememe.getReferencedComponentNid(),
               logicGraphSememe.getAssemblageSequence());
         break;

      case UNKNOWN:
      case RELATIONSHIP_ADAPTOR:  // Dan doesn't believe rel adapaters are ever created / written to ibdf
      default:
         throw new UnsupportedOperationException();
      }

      builder.setPrimordialUuid(version.getPrimordialUuid());
      return builder;
   }

   /**
    * Checks if equivalent.
    *
    * @param ov the ov
    * @param nv the nv
    * @param type the type
    * @return true, if equivalent
    */
   private boolean isEquivalent(StampedVersion ov, StampedVersion nv, OchreExternalizableObjectType type) {
      if ((this.diffOnStatus && (ov.getState() != nv.getState())) ||
            (this.diffOnTimestamp && (ov.getTime() != nv.getTime())) ||
            (this.diffOnAuthor && (ov.getAuthorSequence() != nv.getAuthorSequence())) ||
            (this.diffOnModule && (ov.getModuleSequence() != nv.getModuleSequence())) ||
            (this.diffOnPath && (ov.getPathSequence() != nv.getPathSequence()))) {
         return false;
      } else if (type == OchreExternalizableObjectType.CONCEPT) {
         // No other value to analyze equivalence for a concept, so return
         // true
         return true;
      } else {
         // Analyze Sememe
         final DynamicSememeData[] oldData = getSememeData((SememeVersion) ov);
         final DynamicSememeData[] newData = getSememeData((SememeVersion) nv);

         return isSememeDataEquivalent(oldData, newData);
      }
   }

   /**
    * Gets the new import date.
    *
    * @return the new import date
    */
   public long getNewImportDate() {
      return newImportDate;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the new import date.
    *
    * @param importDate the new new import date
    */
   public void setNewImportDate(String importDate) {
      // Must be in format of 2005-10-06
      try {
         final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

         newImportDate = sdf.parse(importDate)
                            .getTime();
      } catch (final ParseException e) {
         final Date d = new Date();

         newImportDate = d.getTime();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sememe class.
    *
    * @param sememe the sememe
    * @return the sememe class
    */
   private Class<?> getSememeClass(SememeVersion sememe) {
      {
         switch (sememe.getChronology()
                       .getSememeType()) {
         case COMPONENT_NID:
            return ComponentNidSememeImpl.class;

         case DESCRIPTION:
            return DescriptionSememeImpl.class;

         case DYNAMIC:
            return DynamicSememeImpl.class;

         case LONG:
            return LongSememeImpl.class;

         case MEMBER:
            return SememeVersionImpl.class;

         case STRING:
            return StringSememeImpl.class;

         case RELATIONSHIP_ADAPTOR:
            return RelationshipVersionAdaptorImpl.class;

         case LOGIC_GRAPH:
            return LogicGraphSememeImpl.class;

         case UNKNOWN:
         default:
            throw new UnsupportedOperationException();
         }
      }
   }

   /**
    * Gets the sememe data.
    *
    * @param sememe the sememe
    * @return the sememe data
    */
   private DynamicSememeData[] getSememeData(SememeVersion sememe) {
      {
         switch (sememe.getChronology()
                       .getSememeType()) {
         case COMPONENT_NID:
            return new DynamicSememeData[] {
               new DynamicSememeNidImpl(((ComponentNidVersion<?>) sememe).getComponentNid()) };

         case DESCRIPTION:
            return new DynamicSememeData[] { new DynamicSememeStringImpl(((DescriptionVersion) sememe).getText()) };

         case DYNAMIC:
            return ((DynamicSememe<?>) sememe).getData();

         case LONG:
            return new DynamicSememeData[] { new DynamicSememeLongImpl(((LongVersion<?>) sememe).getLongValue()) };

         case MEMBER:
            return new DynamicSememeData[] {};

         case STRING:
            return new DynamicSememeData[] { new DynamicSememeStringImpl(((StringVersion) sememe).getString()) };

         case RELATIONSHIP_ADAPTOR:
            return new DynamicSememeData[] {
               new DynamicSememeStringImpl(((RelationshipVersionAdaptor<?>) sememe).toString()) };

         case LOGIC_GRAPH:
            return new DynamicSememeData[] { new DynamicSememeStringImpl(((LogicGraphVersion) sememe).toString()) };

         case UNKNOWN:
         default:
            throw new UnsupportedOperationException();
         }
      }
   }

   /**
    * Checks if sememe data equivalent.
    *
    * @param oldData the old data
    * @param newData the new data
    * @return true, if sememe data equivalent
    */
   private boolean isSememeDataEquivalent(DynamicSememeData[] oldData, DynamicSememeData[] newData) {
      // Verify same values
      if (oldData.length != newData.length) {
         return false;
      } else {
         for (int i = 0; i < oldData.length; i++) {
            boolean matchFound = false;

            if (((oldData[i] == null) && (newData[i] == null)) ||
                  Arrays.equals(oldData[i].getData(), newData[i].getData())) {
               matchFound = true;
               continue;
            }

            if (!matchFound) {
               return false;
            }
         }

         return true;
      }
   }
}

