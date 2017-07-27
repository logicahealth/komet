package sh.komet.fx.stage;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sh.isaac.api.LookupService;
import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import sh.isaac.komet.iconography.Iconography;
import static sh.isaac.api.constants.Constants.USER_CSS_LOCATION_PROPERTY;
import sh.komet.gui.util.FxGet;

public class MainApp extends Application {
// TODO add TaskProgressView
// http://dlsc.com/2014/10/13/new-custom-control-taskprogressview/
// http://fxexperience.com/controlsfx/features/   
    public static final String SPLASH_IMAGE =
            "prism-splash.png";

    // Create drop label for identified components
    // Create walker panel
    // grow & shrink icons for tabs & tab panels...
    // for each tab group, add a + control to create new tabs...
   @Override
   public void start(Stage stage) throws Exception {
      //TODO have SvgImageLoaderFactory autoinstall as part of a HK2 service. 
      SvgImageLoaderFactory.install();
            

      if (Files.exists(Paths.get("target", "data", "meta-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/data/meta-db.data");
      } else if (Files.exists(Paths.get("target", "data", "solor-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/data/solor-db.data");
      } else if (Files.exists(Paths.get("data", "meta-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "data/meta-db.data");
      } else if (Files.exists(Paths.get("data", "solor-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "data/solor-db.data");
      } else if (Files.exists(Paths.get("meta-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "meta-db.data");
      } else if (Files.exists(Paths.get("solor-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "solor-db.data");
      } else {
         throw new UnsupportedOperationException("Can't find data directory... Working dir: " + System.getProperty("user.dir"));
      }

      if (Files.exists(Paths.get("target", "data", "user.css"))) {
         System.setProperty(USER_CSS_LOCATION_PROPERTY, Paths.get("target", "data", "user.css").toUri().toURL().toString());

      } else if (Files.exists(Paths.get("data", "user.css"))) {
         System.setProperty(USER_CSS_LOCATION_PROPERTY, Paths.get("data", "user.css").toUri().toURL().toString());

      } else if (Files.exists(Paths.get("user.css"))) {
         System.setProperty(USER_CSS_LOCATION_PROPERTY, Paths.get("user.css").toUri().toURL().toString());
      } else {
         throw new UnsupportedOperationException("Can't find user.css file... Working dir: " + System.getProperty("user.dir"));
      }

      LookupService.startupIsaac();
      
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KometStageScene.fxml"));
      
      Parent root = loader.load();
      KometStageController controller = loader.getController();
      root.setId(UUID.randomUUID().toString());
      Scene scene = new Scene(root);
      //GraphController.setSceneForControllers(scene);
      scene.getStylesheets().add(System.getProperty(USER_CSS_LOCATION_PROPERTY));
      scene.getStylesheets().add(Iconography.getStyleSheetStringUrl());

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
      stage.setTitle("KOMET Reflector");
      stage.setScene(scene);
      
      FxGet.statusMessageService().addScene(scene, controller::reportStatus);

      stage.show();

      //ScenicView.show(scene);
   }

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

}
