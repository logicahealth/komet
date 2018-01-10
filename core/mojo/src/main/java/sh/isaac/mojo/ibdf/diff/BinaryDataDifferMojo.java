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



package sh.isaac.mojo.ibdf.diff;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.LookupService;
import sh.isaac.api.externalizable.BinaryDataDifferService;
import sh.isaac.api.externalizable.BinaryDataDifferService.ChangeType;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.mojo.external.QuasiMojo;
import sh.isaac.api.externalizable.IsaacExternalizable;

//~--- classes ----------------------------------------------------------------

/**
 * Examines two ibdf files containing two distinct versions of the same
 * terminology and identifies the new/inactivated/modified content between the
 * two versions.
 *
 * Once identified, a new changeset file is generated containing these changes.
 * This file can then be imported into an existing database contining the old
 * version of the terminology. This will upgrade it to the new terminology.
 *
 * {@link QuasiMojo}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Service(name = "diff-ibdfs")
public class BinaryDataDifferMojo
        extends QuasiMojo {
   /** The diff on status. */
   @Parameter
   private final Boolean diffOnStatus = false;

   /** The diff on timestamp. */
   @Parameter
   private final Boolean diffOnTimestamp = false;

   /** The diff on author. */
   @Parameter
   private final Boolean diffOnAuthor = false;

   /** The diff on module. */
   @Parameter
   private final Boolean diffOnModule = false;

   /** The diff on path. */
   @Parameter
   private final Boolean diffOnPath = false;
   
   @Parameter
   private Boolean createAnalysisFiles = false;
   
   private static final Logger log = LogManager.getLogger();

   @Parameter(required = true)
   private File oldVersionFile;


   @Parameter(required = true)
   private File newVersionFile;


   @Parameter(required = true)
   private String inputAnalysisDir;


   @Parameter(required = true)
   private String comparisonAnalysisDir;

   /** The import date. */
   @Parameter
   private String importDate;

   /** The changeset file name. */
   @Parameter(required = true)
   String deltaIbdfPath;
   
   @Parameter(required = true)
   protected String converterSourceArtifactVersion;

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @Override
   public void execute() throws MojoExecutionException {
      final BinaryDataDifferService differService = LookupService.getService(BinaryDataDifferService.class);

      differService.initialize(comparisonAnalysisDir, 
            inputAnalysisDir, 
            deltaIbdfPath, 
            createAnalysisFiles, 
            diffOnStatus, 
            diffOnTimestamp, 
            diffOnAuthor, 
            diffOnModule, 
            diffOnPath,
            importDate,
            converterSourceArtifactVersion);  //TODO [DAN 3] Jesse had "VHAT " hardcoded here for some silly reason...

      Map<IsaacObjectType, Set<IsaacExternalizable>> oldContentMap = null;
      Map<IsaacObjectType, Set<IsaacExternalizable>> newContentMap = null;
      
         Map<ChangeType, List<IsaacExternalizable>> changedComponents = null;
      
         boolean ranInputAnalysis = false;
         boolean ranOutputAnalysis = false;
      try {
         // Import Input IBDF Files
         log.info("\n\nProcessing Old version IBDF File");
         oldContentMap = differService.processInputIbdfFile(oldVersionFile);
         log.info("\n\nProcessing New version IBDF File");
         newContentMap = differService.processInputIbdfFile(newVersionFile);

               // Transform input old & new content into text & json files
               log.info("\n\nCreating analysis files for input/output files");
               if (createAnalysisFiles) {
                  ranInputAnalysis = true;
                  differService.createAnalysisFiles(oldContentMap, newContentMap, null);
               }
          
               // Execute diff process
               log.info("\n\nRunning Compute Delta");
               changedComponents = differService.computeDelta(oldContentMap, newContentMap);
         
               // Create diff IBDF file
               log.info("\n\nCreating the delta ibdf file");

         differService.generateDeltaIbdfFile(changedComponents);
               // Transform diff IBDF file into text & json files
               log.info("\n\nCreating analysis files for diff file");

         if (this.createAnalysisFiles) {
                     ranOutputAnalysis = true;
                     differService.createAnalysisFiles(null, null, changedComponents);
         }
      } catch (final Exception e) {
         if (createAnalysisFiles && !ranInputAnalysis) {
             differService.createAnalysisFiles(oldContentMap, newContentMap, null);
          }
      if (createAnalysisFiles && !ranOutputAnalysis) {
         differService.createAnalysisFiles(null, null, changedComponents);
      }
         throw new MojoExecutionException(e.getMessage(), e);
      }
   }
}

