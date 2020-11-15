package sh.isaac.komet.importer.menu;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.convert.mojo.turtle.TurtleImportHK2Direct;
import sh.komet.gui.menu.MenuItemWithText;

import java.io.File;
import java.util.Optional;

public class BeerImporter {
    private static final Logger LOG = LogManager.getLogger();

    public static Optional<MenuItem> getBeerMenu(Window parentWindow) {

        File beer = new File("../../integration/tests/src/test/resources/turtle/bevontology-0.8.ttl");
        if (beer.isFile()) {
            // This should only appear if you are running from eclipse / netbeans....
            javafx.scene.control.MenuItem convertBeer = new MenuItemWithText("Beer me!");
            convertBeer.setOnAction((ActionEvent event) -> {
                Get.executor().execute(() -> {
                    try {
                        Transaction transaction = Get.commitService().newTransaction(Optional.of("Import beer"), ChangeCheckerMode.ACTIVE, false);
                        TurtleImportHK2Direct timd = Get.service(TurtleImportHK2Direct.class);
                        timd.configure(null, beer.toPath(), "0.8", null, transaction);
                        timd.convertContent(update -> {
                        }, (work, totalWork) -> {
                        });
                        Optional<CommitRecord> cr = transaction.commit("Beer has arrived!").get();  //TODO [DAN] this is broken, it isn't returning a commit record
                        LOG.error("commit record empty? {}", cr);
                        //if (cr.isPresent()) {
                        Get.indexDescriptionService().refreshQueryEngine();
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Beer has arrived!");
                            alert.setHeaderText("Beer has been imported!");
                            alert.initOwner(parentWindow);
                            alert.setResizable(true);
                            alert.showAndWait();
                        });
                        //}
                    } catch (Exception e) {
                        LOG.error("Beer failure", e);
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Party Foul!");
                            alert.setHeaderText("Something went wrong loading beer!");
                            alert.initOwner(parentWindow);
                            alert.setResizable(true);
                            alert.showAndWait();
                        });
                    }
                });
            });
            return Optional.of(convertBeer);

        }
        return Optional.empty();
    }
}
