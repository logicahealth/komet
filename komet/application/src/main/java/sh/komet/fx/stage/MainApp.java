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



package sh.komet.fx.stage;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Application;
import javafx.application.Platform;

import javafx.concurrent.Task;

import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static javafx.application.Application.launch;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.util.FxGet;

import static sh.isaac.api.constants.Constants.USER_CSS_LOCATION_PROPERTY;

//~--- classes ----------------------------------------------------------------

public class MainApp
        extends Application {
// TODO add TaskProgressView
// http://dlsc.com/2014/10/13/new-custom-control-taskprogressview/
// http://fxexperience.com/controlsfx/features/   
   public static final String SPLASH_IMAGE = "prism-splash.png";

   //~--- methods -------------------------------------------------------------

   /**
    * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the
    * application can not be launched through deployment artifacts, e.g., in IDEs with limited FX support. NetBeans
    * ignores main().
    *
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      launch(args);
   }

   // Create drop label for identified components
   // Create walker panel
   // grow & shrink icons for tabs & tab panels...
   // for each tab group, add a + control to create new tabs...
   @Override
   public void start(Stage stage)
            throws Exception {
      // TODO have SvgImageLoaderFactory autoinstall as part of a HK2 service.
      SvgImageLoaderFactory.install();
      LookupService.startupPreferenceProvider();

      IsaacPreferences appPreferences = Get.applicationPreferences();

      appPreferences.putEnum(DatabaseInitialization.LOAD_METADATA);
      LookupService.startupIsaac();

      if (Get.metadataService()
             .importMetadata()) {
         final StampCoordinate stampCoordinate = Get.coordinateFactory()
                                                    .createDevelopmentLatestStampCoordinate();
         final LogicCoordinate logicCoordinate = Get.coordinateFactory()
                                                    .createStandardElProfileLogicCoordinate();
         final EditCoordinate  editCoordinate  = Get.coordinateFactory()
                                                    .createClassifierSolorOverlayEditCoordinate();
         final ClassifierService logicService = Get.logicService()
                                                   .getClassifierService(
                                                         stampCoordinate,
                                                               logicCoordinate,
                                                               editCoordinate);
         final Task<ClassifierResults> classifyTask      = logicService.classify();
         final ClassifierResults       classifierResults = classifyTask.get();
      }

      FXMLLoader           loader     = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
      Parent               root       = loader.load();
      KometStageController controller = loader.getController();

      root.setId(UUID.randomUUID()
                     .toString());

      Scene scene = new Scene(root);

      // GraphController.setSceneForControllers(scene);
      scene.getStylesheets()
           .add(System.getProperty(USER_CSS_LOCATION_PROPERTY));
      scene.getStylesheets()
           .add(Iconography.getStyleSheetStringUrl());

      // SNAPSHOT
      // Chronology
      // Reflector
      //
      // Logic, Language, Dialect, Chronology,
      // LILAC Reflector (LOGIC,
      // COLLD Reflector: Chronology of Logic, Language, and Dialect : COLLAD
      // COLLDAE Chronology of Logic, Langugage, Dialect, and Extension
      // CHILLDE
      // Knowledge, Language, Dialect, Chronology
      // KOLDAC
      stage.setTitle("SOLOR");
      stage.setScene(scene);
      FxGet.statusMessageService()
           .addScene(scene, controller::reportStatus);
      stage.show();
      stage.setOnCloseRequest(this::handleShutdown);

      // ScenicView.show(scene);
   }

   private void handleShutdown(WindowEvent e) {
      LookupService.shutdownIsaac();
      LookupService.shutdownSystem();
      Platform.exit();
      System.exit(0);
   }
}

