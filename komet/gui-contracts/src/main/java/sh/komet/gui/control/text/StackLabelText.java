package sh.komet.gui.control.text;


import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.StyleClassedTextArea;

public class StackLabelText extends StackPane {

    private static class MyStyleClassedTextArea extends StyleClassedTextArea {

        double computePreferredHeight(double width) {
            return this.computePrefHeight(width);
        }
    }

    private static class MyTextFlow extends TextFlow {

        double computePreferredHeight(double width) {
            return this.computePrefHeight(width);
        }
    }


    Label label = new Label();
    MyStyleClassedTextArea textArea = new MyStyleClassedTextArea();
    MyTextFlow textFlow = new MyTextFlow();

    public StackLabelText() {
        StackPane.setAlignment(label, Pos.TOP_LEFT);
        label.setAlignment(Pos.TOP_LEFT);
        getChildren().add(label);

        StackPane.setAlignment(textArea, Pos.TOP_LEFT);
        textArea.setEditable(false);
        getChildren().add(textArea);

        getStyleClass().addListener((ListChangeListener<? super String>) change -> {
            label.getStyleClass().setAll(change.getList());
        });
        textArea.widthProperty().addListener((observable, oldValue, newValue) -> {
            textFlow.setMaxWidth(newValue.doubleValue());
            textFlow.setPrefWidth(newValue.doubleValue());
            textFlow.setMinWidth(newValue.doubleValue());
            double preferredHeight = textFlow.computePreferredHeight(newValue.doubleValue());
            preferredHeight += 30;
            //System.out.println("Pref height for: " + newValue.doubleValue() + " is: " + preferredHeight);

            textArea.setMinHeight(preferredHeight);
            textArea.setPrefHeight(preferredHeight);
            textArea.setMaxHeight(preferredHeight);
            //System.out.println("Pref width for: " + textArea.getTotalWidthEstimate());
        });
    }

    public void setText(String s) {
        textFlow.getChildren().clear();
        textFlow.getChildren().add(new Text(s));
        textArea.clear();
        textArea.appendText(s);
    }

    public String getText() {
        return textArea.getText();
    }

    public void setWrapText(boolean b) {
        textArea.setWrapText(b);

    }

    public void setImage(Node graphic) {
        label.setGraphic(graphic);
    }

    public void setImageLocation(ContentDisplay position) {
        label.setContentDisplay(position);
    }
}
