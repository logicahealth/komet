/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.search.flwor;

import sh.isaac.api.query.LetItemKey;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class LetItemsController {
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="letAnchorPane"
    private AnchorPane letAnchorPane; // Value injected by FXMLLoader

    @FXML // fx:id="letListViewletListView"
    private ListView<LetItemKey> letListViewletListView; // Value injected by FXMLLoader

    @FXML // fx:id="letItemBorderPane"
    private BorderPane letItemBorderPane; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert letAnchorPane != null : "fx:id=\"letAnchorPane\" was not injected: check your FXML file 'LetItems.fxml'.";
        assert letListViewletListView != null : "fx:id=\"letListViewletListView\" was not injected: check your FXML file 'LetItems.fxml'.";
        assert letItemBorderPane != null : "fx:id=\"letItemBorderPane\" was not injected: check your FXML file 'LetItems.fxml'.";

    }

    public void reset() {
        letListViewletListView.getItems().clear();
    }
    public ListView<LetItemKey> getLetListViewletListView() {
        return letListViewletListView;
    }
    
    public Node getNode() {
        return letAnchorPane;
    }

    public BorderPane getLetItemBorderPane() {
        return letItemBorderPane;
    }
    
    
}
