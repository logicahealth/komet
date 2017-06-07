package sh.komet.fx.stage;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sh.isaac.api.LookupService;
import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import sh.isaac.komet.iconography.Iconography;

public class MainApp extends Application {
// TODO add TaskProgressView
// http://dlsc.com/2014/10/13/new-custom-control-taskprogressview/
// http://fxexperience.com/controlsfx/features/   
   
   @Override
   public void start(Stage stage) throws Exception {
      //TODO have SvgImageLoaderFactory autoinstall as part of a HK2 service. 
      SvgImageLoaderFactory.install();
            
      if (Files.exists(Paths.get("target", "data", "meta-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/data/meta-db.data");
      } else if (Files.exists(Paths.get("target", "data", "solor-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/data/solor-db.data");
      } else if (Files.exists(Paths.get("meta-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "solor-db.data");
      } else if (Files.exists(Paths.get("solor-db.data"))) {
         System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "meta-db.data");
      } else {
         throw new UnsupportedOperationException("Can't find data directory... Working dir: " + System.getProperty("user.dir"));
      }

      LookupService.startupIsaac();
      Parent root = FXMLLoader.load(getClass().getResource("/fxml/KometStageScene.fxml"));

      Scene scene = new Scene(root);
      //GraphController.setSceneForControllers(scene);
      scene.getStylesheets().add("/styles/Styles.css");
      scene.getStylesheets().add(Iconography.getStyleSheetStringUrl());

      stage.setTitle("ISAAC's KOMET");
      stage.setScene(scene);
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
