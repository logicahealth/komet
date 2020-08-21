package sh.komet.gui.control.file;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import org.controlsfx.property.editor.PropertyEditor;
import sh.komet.gui.control.property.wrapper.PropertySheetFileWrapper;

import java.io.File;

public class FilePropertyEditor implements PropertyEditor<String> {

    private final PropertySheetFileWrapper propertySheetFileWrapper;
    private final Label fileString = new Label();
    {
        fileString.setWrapText(true);
        fileString.setPrefWidth(200);
        fileString.setMaxWidth(1000);
        fileString.setPadding(new Insets(0, 4, 0, 0));
    }
    private final Button chooseFile = new Button("select");
    private final GridPane editorPane = new GridPane();

    public FilePropertyEditor(PropertySheetFileWrapper propertySheetFileWrapper) {
        if (propertySheetFileWrapper == null) {
            throw new NullPointerException("propertySheetFileWrapper cannot be null.");
        }
        this.propertySheetFileWrapper = propertySheetFileWrapper;
        if (propertySheetFileWrapper.getValue() != null) {
            fileString.setText(propertySheetFileWrapper.getValue());
        }
        GridPane.setConstraints(fileString, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        editorPane.getChildren().add(fileString);
        GridPane.setConstraints(chooseFile, 1, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
        editorPane.getChildren().add(chooseFile);
        chooseFile.setOnAction(this::selectFile);
    }

    private void selectFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select " + chooseFile.getText());
        if (propertySheetFileWrapper.getValue() != null && (!propertySheetFileWrapper.getValue().isBlank())) {
            File initialFile = new File(propertySheetFileWrapper.getValue());
            fileChooser.setInitialDirectory(initialFile.getParentFile());
            fileChooser.setInitialFileName(initialFile.getName());
        } else {
            String path = System.getProperty("user.home") + File.separator + "Solor";
            File initialFile = new File(path);
            fileChooser.setInitialDirectory(initialFile);
        }

        switch (propertySheetFileWrapper.getFileOperation()) {
            case READ: {
                File selectedFile = fileChooser.showOpenDialog(fileString.getScene().getWindow());
                fileString.setText(selectedFile.getAbsolutePath());
                propertySheetFileWrapper.setValue(selectedFile.getAbsolutePath());
                break;
            }
            case WRITE:
            case OVERWRITE: {
                File selectedFile = fileChooser.showSaveDialog(fileString.getScene().getWindow());
                fileString.setText(selectedFile.getAbsolutePath());
                propertySheetFileWrapper.setValue(selectedFile.getAbsolutePath());
                break;
            }
        }
    }


    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public String getValue() {
        return propertySheetFileWrapper.getValue();
    }

    @Override
    public void setValue(String position) {
        this.propertySheetFileWrapper.setValue(position);
    }

}
