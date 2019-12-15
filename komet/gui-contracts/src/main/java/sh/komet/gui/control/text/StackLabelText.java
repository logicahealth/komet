package sh.komet.gui.control.text;


import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.HitInfo;
import sh.isaac.komet.openjdk.TextAreaNoScrollerSkin;

public class StackLabelText extends StackPane {

    Label label = new Label();
    TextArea textArea;
    ChangeListener<Boolean> listener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
            this.textAreaFocusListener(observable, oldValue, newValue);

    public StackLabelText() {
        StackPane.setAlignment(label, Pos.TOP_LEFT);
        label.setAlignment(Pos.TOP_LEFT);
        getChildren().add(label);
        getStyleClass().addListener((ListChangeListener<? super String>) change -> {
            label.getStyleClass().setAll(change.getList());
        });
        label.setOnMouseClicked(this::mouseClickedInLabel);
    }

    private void mouseClickedInLabel(MouseEvent mouseEvent) {
        if (label.getGraphic() != null) {
            return;
        }
        textArea = new TextArea();

        textArea.setEditable(false);
        TextAreaNoScrollerSkin skin = new TextAreaNoScrollerSkin(textArea);
        textArea.setSkin(skin);

        StackPane.setAlignment(textArea, Pos.TOP_LEFT);

        textArea.setText(label.getText());
        textArea.getStyleClass().setAll(label.getStyleClass());
        textArea.setMinSize(label.getWidth(), label.getHeight());
        textArea.setPrefSize(label.getWidth(), label.getHeight());
        textArea.setMaxSize(label.getWidth(), label.getHeight());
        getChildren().add(textArea);
        textArea.requestFocus();
        HitInfo hitInfo = skin.getIndex(mouseEvent.getX(), mouseEvent.getY());
        skin.positionCaret(hitInfo, false);

        textArea.focusedProperty().addListener(this.listener);
    }

    private void textAreaFocusListener(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!newValue) {
            textArea.focusedProperty().removeListener(this.listener);
            textArea = null;
            getChildren().clear();
            getChildren().add(label);
         }
    }

    public StringProperty textProperty() {
        return label.textProperty();
    }

    public void setText(String s) {
        label.setText(s);
    }

    public String getText() {
        return label.getText();
    }

    public void setWrapText(boolean b) {
        label.setWrapText(b);
    }

    public void setImage(Node graphic) {
        label.setGraphic(graphic);
    }

    public void setImageLocation(ContentDisplay position) {
        label.setContentDisplay(position);
    }
}
