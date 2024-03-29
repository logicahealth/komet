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
package sh.isaac.komet.statement;


import java.net.URL;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import sh.isaac.model.statement.ClinicalStatementImpl;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

public class StatementViewController {

    private static final Logger LOG = LogManager.getLogger();
    
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private BorderPane borderPane;

    @FXML // fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader
    
    private ViewProperties viewProperties;
    
    SimpleObjectProperty<ClinicalStatementImpl> clinicalStatement = new SimpleObjectProperty<>();
    
    StatementPropertySheet statementPropertySheet;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'StatementView.fxml'.";
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'StatementView.fxml'.";
        clinicalStatement.addListener((observable, oldValue, newValue) -> {
            statementPropertySheet.setClinicalStatement(newValue);
        });
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        statementPropertySheet = new StatementPropertySheet(this.viewProperties);
        borderPane.setCenter(statementPropertySheet.getPropertySheet());
    }

    public ClinicalStatementImpl getClinicalStatement() {
        return clinicalStatement.get();
    }

    public SimpleObjectProperty<ClinicalStatementImpl> clinicalStatementProperty() {
        return clinicalStatement;
    }

    public void setClinicalStatement(ClinicalStatementImpl clinicalStatement) {
        this.clinicalStatement.set(clinicalStatement);
    }
   public void handleRefreshUserCss(ActionEvent event) {
        // "Feature" to make css editing/testing easy in the dev environment. 
        rootPane.getScene()
                .getStylesheets()
                .remove(FxGet.fxConfiguration().getUserCSSURL().toString());
        rootPane.getScene()
                .getStylesheets()
                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
        LOG.debug("Updated css for statement: " + FxGet.fxConfiguration().getUserCSSURL().toString());
    }
}
