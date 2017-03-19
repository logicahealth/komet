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
 * {@link BinaryDataDifferProvider}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Contract
public interface BinaryDataDifferService {
   public enum ChangeType {
      NEW_COMPONENTS,
      RETIRED_COMPONENTS,
      MODIFIED_COMPONENTS;
   }

   //~--- methods -------------------------------------------------------------

   public void generateDiffedIbdfFile(Map<ChangeType, List<OchreExternalizable>> changedComponents)
            throws Exception;

   public Map<ChangeType, List<OchreExternalizable>> identifyVersionChanges(Map<OchreExternalizableObjectType,
              Set<OchreExternalizable>> oldContentMap,
         Map<OchreExternalizableObjectType, Set<OchreExternalizable>> newContentMap);

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

   public Map<OchreExternalizableObjectType, Set<OchreExternalizable>> processVersion(File versionFile)
            throws Exception;

   public void writeFilesForAnalysis(Map<OchreExternalizableObjectType, Set<OchreExternalizable>> newContentMap,
                                     Map<OchreExternalizableObjectType, Set<OchreExternalizable>> oldContentMap,
                                     Map<ChangeType, List<OchreExternalizable>> changedComponents,
                                     String ibdfFileOutputDir,
                                     String analysisFilesOutputDir);
}

