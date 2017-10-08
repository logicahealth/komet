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



package sh.isaac.api.externalizable;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.List;
import java.util.Map;
import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * HK2 Service Contract for BinaryDataDifferProvider
 *
 * {@link BinaryDataDifferProvider}.
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Contract
public interface BinaryDataDifferService {
   /**
    * The Enum ChangeType.
    */
   public enum ChangeType {
      /** The new components. */
      NEW_COMPONENTS,

      /** The retired components. */
      RETIRED_COMPONENTS,

      /** The modified components. */
      MODIFIED_COMPONENTS;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Generate diffed ibdf file.
    *
    * @param changedComponents the changed components
    * @throws Exception the exception
    */
   public void generateDiffedIbdfFile(Map<ChangeType, List<IsaacExternalizable>> changedComponents)
            throws Exception;

   /**
    * Identify version changes.
    *
    * @param oldContentMap the old content map
    * @param newContentMap the new content map
    * @return the map
    */
   public Map<ChangeType, List<IsaacExternalizable>> identifyVersionChanges(Map<IsaacObjectType,
              Set<IsaacExternalizable>> oldContentMap,
         Map<IsaacObjectType, Set<IsaacExternalizable>> newContentMap);

   /**
    * Initialize.
    *
    * @param analysisFilesOutputDir the analysis files output dir
    * @param ibdfFileOutputDir the ibdf file output dir
    * @param changesetFileName the changeset file name
    * @param createAnalysisFiles the create analysis files
    * @param diffOnStatus the diff on status
    * @param diffOnTimestamp the diff on timestamp
    * @param diffOnAuthor the diff on author
    * @param diffOnModule the diff on module
    * @param diffOnPath the diff on path
    * @param importDate the import date
    */
   public void initialize(String analysisFilesOutputDir,
                          String ibdfFileOutputDir,
                          String changesetFileName,
                          Boolean createAnalysisFiles,
                          boolean diffOnStatus,
                          boolean diffOnTimestamp,
                          boolean diffOnAuthor,
                          boolean diffOnModule,
                          boolean diffOnPath,
                          String importDate);

   /**
    * Process version.
    *
    * @param versionFile the version file
    * @return the map
    * @throws Exception the exception
    */
   public Map<IsaacObjectType, Set<IsaacExternalizable>> processVersion(File versionFile)
            throws Exception;

   /**
    * Write files for analysis.
    *
    * @param newContentMap the new content map
    * @param oldContentMap the old content map
    * @param changedComponents the changed components
    * @param ibdfFileOutputDir the ibdf file output dir
    * @param analysisFilesOutputDir the analysis files output dir
    */
   public void writeFilesForAnalysis(Map<IsaacObjectType, Set<IsaacExternalizable>> newContentMap,
                                     Map<IsaacObjectType, Set<IsaacExternalizable>> oldContentMap,
                                     Map<ChangeType, List<IsaacExternalizable>> changedComponents,
                                     String ibdfFileOutputDir,
                                     String analysisFilesOutputDir);
}

