/**
 * Sample Skeleton for 'SelectDataSource.fxml' Controller Class
 */

package sh.komet.fx.stage;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sh.isaac.api.Get;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.util.NaturalOrder;
import sh.komet.gui.util.FxGet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SelectDataSourceController {

    public static final String TARGET_DATABASE = "target database";

    private enum DataSource { Folder, Database, Environment};

    private File rootFolder = new File(System.getProperty("user.home"), "Solor");

    private File workingFolder = new File(System.getProperty("user.dir"), "target");

    private MainApp mainApp;

    private DatabaseLoginController databaseLoginController;

    private AnchorPane databaseLoginRoot;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="dataSourceChoiceBox"
    private ChoiceBox<DataSource> dataSourceChoiceBox; // Value injected by FXMLLoader

    @FXML // fx:id="cancelButton"
    private Button cancelButton; // Value injected by FXMLLoader

    @FXML // fx:id="rootBorderPane"
    private BorderPane rootBorderPane; // Value injected by FXMLLoader

    @FXML // fx:id="fileListView"
    private ListView<String> fileListView; // Value injected by FXMLLoader

    @FXML
    void cancelButtonPressed(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void okButtonPressed(ActionEvent event) {
        switch (dataSourceChoiceBox.getValue()) {
            case Database:
                System.setProperty("ISAAC_PSQL_URL", databaseLoginController.getDatabaseUrl());
                System.setProperty("ISAAC_PSQL_UNAME", databaseLoginController.getUsername());
                System.setProperty("ISAAC_PSQL_UPWD", databaseLoginController.getPassword());
                Get.setUseLuceneIndexes(false);
                new Thread(new StartupAfterSelection(this.getMainApp(), true), "Startup service").start();
                break;
            case Folder:
                String selection = fileListView.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    if (selection.equals(TARGET_DATABASE)) {
                        File selectedFolder = new File(workingFolder, "data" + File.separator + "isaac.data");
                        System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, selectedFolder.getAbsolutePath());
                    } else {
                        File selectedFolder = new File(rootFolder, selection);
                        File[] dataMatches = selectedFolder.listFiles((dir, name) -> name.equals("data"));
                        if (dataMatches.length == 1) {
                            File dataFolder = new File(rootFolder, selection + File.separator + "data");
                            System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, dataFolder.getAbsolutePath());
                        } else {
                            File[] chronologyMatches = selectedFolder.listFiles((dir, name) -> name.equals("chronologies"));
                            if (chronologyMatches.length == 1) {
                                System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, selectedFolder.getAbsolutePath());
                            }
                        }
                    }
                    if (System.getProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY) != null) {
                        new Thread(new StartupAfterSelection(this.getMainApp(), true), "Startup service").start();
                    } else {
                        FxGet.dialogs().showInformationDialog("No folder selected", "Please select a folder or select cancel to quit KOMET");
                    }


                }
                break;
            case Environment:
                new Thread(new StartupAfterSelection(this.getMainApp(), true), "Startup service").start();
                break;
        }
    }

    void dataSourceChanged(ActionEvent event) {
        switch (dataSourceChoiceBox.getValue()) {
            case Database:
                setupDatabase();
                break;
            case Folder:
                setupFolderList();
                break;
            case Environment:
                showEnvironmentVariables();
        }
    }


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        try {
            assert dataSourceChoiceBox != null : "fx:id=\"dataSourceChoiceBox\" was not injected: check your FXML file 'SelectDataSource.fxml'.";
            assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'SelectDataSource.fxml'.";
            dataSourceChoiceBox.setItems(FXCollections.observableArrayList(DataSource.values()));
            dataSourceChoiceBox.getSelectionModel().select(DataSource.Folder);
            setupFolderList();
            dataSourceChoiceBox.setOnAction(this::dataSourceChanged);

            FXMLLoader databaseLoginLoader = new FXMLLoader(getClass().getResource("/fxml/DatabaseLogin.fxml"));
            databaseLoginRoot = databaseLoginLoader.load();
            databaseLoginController = databaseLoginLoader.getController();
            databaseLoginController.setUsername("isaac_user");
            databaseLoginController.setPassword("isaac_pwd");
            databaseLoginController.setDatabaseUrl("jdbc:postgresql://localhost/isaac_db");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    void setupDatabase() {
        rootBorderPane.setCenter(databaseLoginRoot);
    }


    void setupFolderList() {
        rootBorderPane.setCenter(fileListView);
        fileListView.getItems().clear();
        if (workingFolder.exists()) {
            fileListView.getItems().add(TARGET_DATABASE);
        }

        if(!rootFolder.exists()){
            rootFolder.mkdirs();
        }

        for (File f: rootFolder.listFiles()) {
            if (f.isDirectory()) {
                String[] children = f.list((dir, name) -> name.equals("data") || name.equals("chronologies"));
                if (children.length != 0) {
                    fileListView.getItems().add(f.getName());
                }

            }
        }
        fileListView.getItems().sort(new NaturalOrder());
        fileListView.getSelectionModel().selectFirst();

    }

    private void showEnvironmentVariables() {
        ListView<String> environmentListView = new ListView<>();
        rootBorderPane.setCenter(environmentListView);
        environmentListView.getItems().add("SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY"
                + ": " + System.getProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY));

    }


    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
