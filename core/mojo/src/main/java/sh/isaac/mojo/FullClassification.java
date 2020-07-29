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



package sh.isaac.mojo;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.apache.maven.plugin.MojoExecutionException;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.logic.LogicService;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.mojo.external.QuasiMojo;

//~--- classes ----------------------------------------------------------------

/**
 * The Class FullClassification.
 *
 * @author kec
 */
@Service(name = "full-classification")
public class FullClassification
        extends QuasiMojo {
   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      try {
         final LogicService    logicService    = LookupService.getService(LogicService.class);
         EditCoordinate        editCoordinate  = EditCoordinates.getDefaultUserSolorOverlay();
         final LogicCoordinate logicCoordinate = Coordinates.Logic.ElPlusPlus();

         editCoordinate = EditCoordinateImmutable.make(logicCoordinate.getClassifierNid(),
               editCoordinate.getDefaultModuleNid(),
               editCoordinate.getPromotionPathNid(),
                 editCoordinate.getDestinationModuleNid());

         final Task<ClassifierResults> classifyTask =
            logicService.getClassifierService(Coordinates.Manifold.DevelopmentStatedRegularNameSort())
                        .classify();

         classifyTask.get();
      } catch (InterruptedException | ExecutionException ex) {
         throw new MojoExecutionException(ex.toString(), ex);
      }
   }
}

