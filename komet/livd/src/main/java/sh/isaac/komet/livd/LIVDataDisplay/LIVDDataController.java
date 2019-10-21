/**
 * Sample Skeleton for 'ExportSpecification.fxml' Controller Class
 */

package sh.isaac.komet.livd.LIVDataDisplay;


import com.sun.javafx.font.directwrite.RECT;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.apache.batik.css.engine.value.ComputedValue;
import sh.komet.gui.manifold.Manifold;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;


public class LIVDDataController {

    private final int panelHeight = 100;
    private final int badgewidth = 10;

    @FXML
    private Accordion mainAccordian;

    @FXML
    BorderPane topBorderPane;

    @FXML
    ScrollPane scrollPane;

    public LIVDDataController() {

    }


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

    }

    //Code to render content
    public void setManifold(Manifold manifold) {

        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        mainAccordian = new Accordion();
        TitledPane titledPane;
        VBox vBox;

        for(int x = 0; x < 20; x++){

            titledPane = CreateTitlePane();

            vBox = new VBox(5);

            //Create IVD and Loinc Panel
            vBox.getChildren().addAll(createIVDPanel(), new Separator(Orientation.HORIZONTAL), createLoincPanel());

            titledPane.setContent(vBox);

            mainAccordian.getPanes().addAll(titledPane);
        }

        scrollPane.setContent(mainAccordian);
    }

    private Pane createIVDPanel(){
        Pane temp = new Pane();
        temp.setMinHeight(panelHeight);
        temp.setPadding(new Insets(10));

        temp.getChildren().add(CreateBadge("#29a9e1"));

        Button button1 = new Button("Button 1");
        Button button2 = new Button("Button 2");
        Button button3 = new Button("Button 3");
        Button button4 = new Button("Button 4");
        Button button5 = new Button("Button 5");
        Button button6 = new Button("Button 6");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(button1, 0, 0, 1, 1);
        gridPane.add(button2, 1, 0, 1, 1);
        gridPane.add(button3, 2, 0, 1, 1);
        gridPane.add(button4, 0, 1, 1, 1);
        gridPane.add(button5, 1, 1, 1, 1);
        gridPane.add(button6, 2, 1, 1, 1);

        temp.getChildren().add(gridPane);

        return temp;
    }

    private Pane createLoincPanel(){
        Pane temp = new Pane();
        temp.setMinHeight(panelHeight);

        temp.getChildren().add(CreateBadge("#e06328"));
        return temp;
    }

    private Pane CreateBadge(String color){
        Pane sidebar = new Pane();
        sidebar.setMinWidth(badgewidth);
        sidebar.setMinHeight(panelHeight);
        sidebar.setStyle("-fx-background-color:" + color);
        return sidebar;
    }

    private TitledPane CreateTitlePane(){
        TitledPane temp = new TitledPane();
        temp.setText("JAVA");
        temp.setStyle("-fx-background-color: #c3cdd3");
        return temp;
    }
}

