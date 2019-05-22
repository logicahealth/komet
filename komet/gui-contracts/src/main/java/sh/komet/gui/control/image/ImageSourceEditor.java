package sh.komet.gui.control.image;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.controlsfx.property.editor.PropertyEditor;
import sh.komet.gui.util.FxGet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class ImageSourceEditor implements PropertyEditor<byte[]> {

    ObjectProperty<byte[]> imageDataProperty;

    ImageView imageView = new ImageView();
    TextField imageSource = new TextField();
    Button selectFile = new Button("Select file...");
    ToolBar toolBar = new ToolBar(imageSource, selectFile);
    BorderPane editorPane = new BorderPane(imageView);
    {
        imageSource.setPrefColumnCount(25);
        editorPane.setBottom(toolBar);
        selectFile.setOnAction(this::setDataLocation);
        imageSource.textProperty().addListener(this::imageSourceChanged);
    }

    public ImageSourceEditor(ObjectProperty<byte[]> imageDataProperty) {
        this.imageDataProperty = imageDataProperty;
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public byte[] getValue() {
        return imageDataProperty.getValue();
    }

    @Override
    public void setValue(byte[] value) {
        imageDataProperty.setValue(value);
    }

    void imageSourceChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        validUrl(newValue).ifPresent(url -> {
            try {
                imageView.setImage(new Image(url.toExternalForm()));
                try (InputStream urlStream = url.openStream()) {
                    imageDataProperty.setValue(urlStream.readAllBytes());
                }
            } catch (IOException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        });
    }

    public static Optional<URL> validUrl(String urlStr)
    {
        try {
            URL url = new URL(urlStr);
            url.toURI();
            return Optional.of(url);
        }
        catch (URISyntaxException exception) {
            return Optional.empty();
        }
        catch (MalformedURLException exception) {
            return Optional.empty();
        }
    }
    void setDataLocation(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image files", "*.bmp", "*.gif", "*.jpeg", "*.jpg", "*.png"));
        File selectedFile = fileChooser.showOpenDialog(toolBar.getScene().getWindow());
        if (selectedFile != null) {
            try {
                imageSource.setText(selectedFile.toURI().toURL().toExternalForm());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }

        }
    }

}
