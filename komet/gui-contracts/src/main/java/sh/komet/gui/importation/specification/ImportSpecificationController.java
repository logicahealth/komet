package sh.komet.gui.importation.specification;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 2019-05-01
 * aks8m - https://github.com/aks8m
 */
public class ImportSpecificationController {

    private Manifold manifold;
    private final SimpleListProperty<Path> pathsSelectedForImport = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<Path> pathsNotMatchedForImport = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleMapProperty<SupportedConverterTypes, Path> pathsToImport = new SimpleMapProperty<>(FXCollections.observableMap(new TreeMap<>()));

    @FXML
    private Button browseFiles;

    @FXML
    private BorderPane importSpecificationBorderPane;


    @FXML
    private void initialize() {

    }

    @FXML
    private void browseForFiles(ActionEvent actionEvent){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Specify file(s) for import...");

        List<Path> paths = chooser.showOpenMultipleDialog(importSpecificationBorderPane.getScene().getWindow()).stream()
                .map(File::toPath)
                .collect(Collectors.toList());

        if(paths.size() > 0) {
            this.pathsSelectedForImport.get().addAll(paths);
            matchSelectedPathsForImport();
        }
    }

    private void matchSelectedPathsForImport(){
        SupportedConverterTypes.topologySortBySourceDependencies().stream()
                .forEach(importer ->
                    this.pathsSelectedForImport.get().stream()
                            .forEach(path -> {
                                SupportedConverterTypes supportedConverterType = SupportedConverterTypes.findByPath(path, true);
                                if( supportedConverterType != null){
                                    this.pathsToImport.put(supportedConverterType, path);
                                }
                            }));
        /**
         * Use enum in this class. Have a switch statement that says what
         */
        System.out.println("break");
    }

    @FXML
    private void runImportSpecification(ActionEvent actionEvent){

    }

    public void setManifold(Manifold manifold){
        this.manifold = manifold;
    }

}
