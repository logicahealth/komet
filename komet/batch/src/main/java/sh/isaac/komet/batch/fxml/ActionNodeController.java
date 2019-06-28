
package sh.isaac.komet.batch.fxml;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.komet.batch.ActionCell;
import sh.komet.gui.cell.list.ConceptCell;
import sh.komet.gui.control.concept.ConceptSpecificationEditor;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;

public class ActionNodeController {

    @FXML
    private AnchorPane anchorPane;

    ActionCell actionCell;

    @FXML
    private ComboBox<Integer> fieldCombo;

    @FXML
    private ComboBox<Integer> forCombo;

    @FXML
    private AnchorPane valueAnchorPane;

    private Manifold manifold;

    private PropertySheetItemConceptWrapper conceptWrapper;

    private SimpleObjectProperty<ConceptSpecification> conceptProperty = new SimpleObjectProperty<>(this, MetaData.CONCEPT_FIELD____SOLOR.toExternalString());


    @FXML
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'ActionNode.fxml'.";
        assert fieldCombo != null : "fx:id=\"fieldCombo\" was not injected: check your FXML file 'ActionNode.fxml'.";
        assert forCombo != null : "fx:id=\"forCombo\" was not injected: check your FXML file 'ActionNode.fxml'.";
        assert valueAnchorPane != null : "fx:id=\"valueAnchorPane\" was not injected: check your FXML file 'ActionNode.fxml'.";
    }

    @FXML
    void delete(ActionEvent event) {
        this.actionCell.delete(event);
    }

    @FXML
    void moveDown(ActionEvent event) {
        this.actionCell.moveDown(event);
    }

    @FXML
    void moveUp(ActionEvent event) {
        this.actionCell.moveUp(event);
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public void setActionCell(ActionCell actionCell) {
        this.actionCell = actionCell;
    }

    public ActionCell getActionCell() {
        return actionCell;
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        forCombo.setCellFactory(param -> new ConceptCell(this.manifold));
        forCombo.setButtonCell(new ConceptCell(this.manifold));
        fieldCombo.setCellFactory(param -> new ConceptCell(this.manifold));
        fieldCombo.setButtonCell(new ConceptCell(this.manifold));


        ObservableList<Integer> options =
                FXCollections.observableArrayList(TermAux.ANY_ASSEMBLAGE.getNid());
        for (int nid: Get.assemblageService().getAssemblageConceptNids()) {
            options.add(nid);
        }

        forCombo.setItems(options);
        forCombo.getSelectionModel().select(0);

        ObservableList<Integer> fields =
                FXCollections.observableArrayList();
        fields.add(ObservableVersion.PROPERTY_INDEX.STATUS.getSpec().getNid());
        fields.add(ObservableVersion.PROPERTY_INDEX.MODULE.getSpec().getNid());
        fields.add(ObservableVersion.PROPERTY_INDEX.PATH.getSpec().getNid());

        fieldCombo.setItems(fields);
        fieldCombo.getSelectionModel().select((Integer) ObservableVersion.PROPERTY_INDEX.PATH.getSpec().getNid());

        conceptWrapper = new PropertySheetItemConceptWrapper(this.manifold,
                conceptProperty, TermAux.MASTER_PATH.getNid(), TermAux.DEVELOPMENT_PATH.getNid());
        conceptWrapper.setDefaultValue(TermAux.DEVELOPMENT_PATH);
        ConceptSpecificationEditor conceptSpecificationEditor = new ConceptSpecificationEditor(conceptWrapper, this.manifold);
        Node editor = conceptSpecificationEditor.getEditor();
        AnchorPane.setTopAnchor(editor, 0.0);
        AnchorPane.setRightAnchor(editor, 0.0);
        AnchorPane.setBottomAnchor(editor, 0.0);
        AnchorPane.setLeftAnchor(editor, 0.0);
        valueAnchorPane.getChildren().add(editor);
    }
}
