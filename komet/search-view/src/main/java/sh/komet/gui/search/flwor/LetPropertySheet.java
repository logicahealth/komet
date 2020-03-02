package sh.komet.gui.search.flwor;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.manifold.Manifold;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableConceptProxy;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.ManifoldCoordinateForQuery;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl; 
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampCoordinateImpl;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author aks8m
 */
public class LetPropertySheet {

    private static final Logger LOG = LogManager.getLogger();

    private final BorderPane propertySheetBorderPane = new BorderPane();
    private final ObservableList<PropertySheet.Item> items;
    private final Manifold manifoldForDisplay;
    private final MenuButton addLetClauseButton = new MenuButton("Add let clause...");
    private final Button removeLetClause = new Button("Remove let clause");
    {
        removeLetClause.setOnAction(this::removeClause);
    }
    private final ToolBar letToolbar = new ToolBar(addLetClauseButton, removeLetClause);

    {
        propertySheetBorderPane.setTop(letToolbar);
    }
    private LetItemsController letItemsController;
    private final HashMap<LetItemKey, LetItemPanel> letItemPanelMap = new HashMap();
    private final ObservableMap<LetItemKey, Object> letItemObjectMap = FXCollections.observableHashMap();

    private final ObservableList<LetItemKey> stampCoordinateKeys = FXCollections.observableArrayList();
    private final ObservableList<LetItemKey> languageCoordinateKeys = FXCollections.observableArrayList();
    private final ObservableList<LetItemKey> logicCoordinateKeys = FXCollections.observableArrayList();
    private final ObservableList<LetItemKey> manifoldCoordinateKeys = FXCollections.observableArrayList();
    private final ObservableList<LetItemKey> conceptSpecificationKeys = FXCollections.observableArrayList();
    private final ObservableList<LetItemKey> stringKeys = FXCollections.observableArrayList();

    private final FLWORQueryController fLWORQueryController;

    public LetPropertySheet(Manifold manifold, FLWORQueryController fLWORQueryController) {
        this.manifoldForDisplay = manifold;
        this.fLWORQueryController = fLWORQueryController;
        this.items = FXCollections.observableArrayList();
        MenuItem addStampCoordinate = new MenuItemWithText("Add stamp coordinate");
        addStampCoordinate.setOnAction(this::addStampCoordinate);
        this.addLetClauseButton.getItems().add(addStampCoordinate);

        MenuItem addLanguageCoordinate = new MenuItemWithText("Add language coordinate");
        addLanguageCoordinate.setOnAction(this::addLanguageCoordinate);
        this.addLetClauseButton.getItems().add(addLanguageCoordinate);

        MenuItem addLogicCoordinate = new MenuItemWithText("Add logic coordinate");
        addLogicCoordinate.setOnAction(this::addLogicCoordinate);
        this.addLetClauseButton.getItems().add(addLogicCoordinate);

        MenuItem addManifoldCoordinate = new MenuItemWithText("Add manifold coordinate");
        addManifoldCoordinate.setOnAction(this::addManifoldCoordinate);
        this.addLetClauseButton.getItems().add(addManifoldCoordinate);
        
        MenuItem addConceptSpecification = new MenuItemWithText("Add concept specification");
        addConceptSpecification.setOnAction(this::addConceptSpecification);
        this.addLetClauseButton.getItems().add(addConceptSpecification);
                
        MenuItem addString = new MenuItemWithText("Add string");
        addString.setOnAction(this::addString);
        this.addLetClauseButton.getItems().add(addString);
                
        AnchorPane.setBottomAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setTopAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setLeftAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setRightAnchor(this.propertySheetBorderPane, 0.0);

        setupLetItemSubLists();
        this.letItemObjectMap.addListener(this::letItemsChanged);
    }

    public ObservableList<LetItemKey> getStampCoordinateKeys() {
        return this.stampCoordinateKeys;
    }

    public ObservableList<LetItemKey> getLanguageCoordinateKeys() {
        return this.languageCoordinateKeys;
    }

    public ObservableList<LetItemKey> getLogicCoordinateKeys() {
        return this.logicCoordinateKeys;
    }

    public ObservableList<LetItemKey> getConceptSpecificationKeys() {
        return this.conceptSpecificationKeys;
    }

    public ObservableList<LetItemKey> getManifoldCoordinateKeys() {
        return this.manifoldCoordinateKeys;
    }

    public ObservableList<LetItemKey> getStringKeys() {
        return this.stringKeys;
    }

    private void setupLetItemSubLists() {
        this.stampCoordinateKeys.clear();
        this.languageCoordinateKeys.clear();
        this.logicCoordinateKeys.clear();
        this.manifoldCoordinateKeys.clear();
        this.conceptSpecificationKeys.clear();
        for (Map.Entry<LetItemKey, Object> entry : this.letItemObjectMap.entrySet()) {
            if (entry.getValue() instanceof StampCoordinate
                    && !(entry.getValue() instanceof ManifoldCoordinate)) {
                if (!this.stampCoordinateKeys.contains(entry.getKey())) {
                    this.stampCoordinateKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof LanguageCoordinate
                    && !(entry.getValue() instanceof ManifoldCoordinate)) {
                if (!this.languageCoordinateKeys.contains(entry.getKey())) {
                    this.languageCoordinateKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof LogicCoordinate
                    && !(entry.getValue() instanceof ManifoldCoordinate)) {
                if (!this.logicCoordinateKeys.contains(entry.getKey())) {
                    this.logicCoordinateKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof ManifoldCoordinate) {
                if (!this.manifoldCoordinateKeys.contains(entry.getKey())) {
                    this.manifoldCoordinateKeys.add(entry.getKey());
                }
                if (entry.getValue() instanceof ManifoldCoordinateForQuery) {
                    ((ManifoldCoordinateForQuery) entry.getValue()).setQuery(fLWORQueryController.getQuery());
                }
            }
            if (entry.getValue() instanceof ConceptSpecification) {
                if (!this.conceptSpecificationKeys.contains(entry.getKey())) {
                    this.conceptSpecificationKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof String) {
                if (!this.stringKeys.contains(entry.getKey())) {
                    this.stringKeys.add(entry.getKey());
                }
            }
        }
    }

    private void letItemsChanged(MapChangeListener.Change<? extends LetItemKey, ? extends Object> change) {
        setupLetItemSubLists();
    }

    public void addLanguageCoordinate(LetItemKey newLetItem, LanguageCoordinate newLanguageCoordinate) {
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        ObservableLanguageCoordinate languageCoordinate;
        if (newLanguageCoordinate instanceof ObservableLanguageCoordinate) {
            languageCoordinate = (ObservableLanguageCoordinate) newLanguageCoordinate;
        } else {
            languageCoordinate = new ObservableLanguageCoordinateImpl(newLanguageCoordinate);
        }
        this.letItemObjectMap.put(newLetItem, languageCoordinate);
        LetItemPanel newLetItemPanel = new LetItemPanel(this.manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), languageCoordinate, this);
        this.letItemPanelMap.put(newLetItem, newLetItemPanel);

        this.letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }

    public void reset() {
        this.letItemPanelMap.clear();
        this.stampCoordinateKeys.clear();
        this.languageCoordinateKeys.clear();
        this.letItemsController.reset();
        this.letItemObjectMap.clear();
    }
    
    public void addItem(LetItemKey newLetItem, Object newObject) {
        if (newObject instanceof StampCoordinate
                && !(newObject instanceof ManifoldCoordinate)) {
            addStampCoordinate(newLetItem, (StampCoordinate) newObject);
        } else if (newObject instanceof String) {
            addString(newLetItem, (String) newObject);
        } else if (newObject instanceof LanguageCoordinate
                && !(newObject instanceof ManifoldCoordinate)) {
            addLanguageCoordinate(newLetItem, (LanguageCoordinate) newObject);
        } else if (newObject instanceof LogicCoordinate
                && !(newObject instanceof ManifoldCoordinate)) {
            addLogicCoordinate(newLetItem, (LogicCoordinate) newObject);
        } else if (newObject instanceof ManifoldCoordinate) {
            addManifoldCoordinate(newLetItem, (ManifoldCoordinateForQuery) newObject);
        } else if (newObject instanceof ConceptProxy) {
            ObservableConceptProxy newObjectProxy = new ObservableConceptProxy(newObject, TermAux.CONCEPT_FIELD.toExternalString(), (ConceptProxy) newObject);
            addConceptSpecification(newLetItem, newObjectProxy);
        } else if (newObject instanceof ObservableConceptProxy) {
            ObservableConceptProxy newObjectProxy = (ObservableConceptProxy) newObject;
            addConceptSpecification(newLetItem, newObjectProxy);
        } else {
            this.letItemObjectMap.put(newLetItem, newObject);
            FxGet.dialogs().showInformationDialog("Unsupported let item", "Can't create panel for " + newLetItem + ": " + newObject);
        }
    }
    public void removeClause(ActionEvent action) {

        LetItemKey selectedLetItem = this.letItemsController.getLetListViewletListView().getSelectionModel().getSelectedItem();
        if (selectedLetItem != null) {
            this.letItemPanelMap.remove(selectedLetItem);
            this.letItemsController.getLetListViewletListView().getItems().remove(selectedLetItem);
            this.letItemsController.getLetItemBorderPane().setCenter(null);
            this.letItemObjectMap.remove(selectedLetItem);
        }
    }
    public void addString(ActionEvent action) {
        LetItemKey newLetItem = new LetItemKey(createUniqueKey("String"));
        addString(newLetItem, "edit-me");
    }

    public void addString(LetItemKey newLetItem, String string) {
        SimpleStringProperty stringProperty = new SimpleStringProperty(this, MetaData.STRING____SOLOR.toExternalString(), string);

        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        this.letItemObjectMap.put(newLetItem, string);
        stringProperty.addListener((observable, oldValue, newValue) -> {
            this.letItemObjectMap.put(newLetItem, string);
        });
        LetItemPanel newLetItemPanel = new LetItemPanel(this.manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), stringProperty, this);
        this.letItemPanelMap.put(newLetItem, newLetItemPanel);

        this.letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }

    public void addLanguageCoordinate(ActionEvent action) {
        LetItemKey newLetItem = new LetItemKey(createUniqueKey("[US, UK] English"));
        addLanguageCoordinate(newLetItem, this.manifoldForDisplay.getLanguageCoordinate().deepClone());
    }

    public void addManifoldCoordinate(ActionEvent action) {

        LetItemKey newLetItem = new LetItemKey(createUniqueKey("Manifold"));

        ManifoldCoordinateForQuery manifoldCoordinate = new ManifoldCoordinateForQuery();
        if (!this.stampCoordinateKeys.isEmpty()) {
            manifoldCoordinate.setOriginStampCoordinateKey(this.stampCoordinateKeys.get(0));
        }
        if (!this.languageCoordinateKeys.isEmpty()) {
            manifoldCoordinate.setLanguageCoordinateKey(this.languageCoordinateKeys.get(0));
        }
        if (!this.logicCoordinateKeys.isEmpty()) {
            manifoldCoordinate.setLogicCoordinateKey(this.logicCoordinateKeys.get(0));
        }
        manifoldCoordinate.setQuery(this.fLWORQueryController.getQuery());
        addManifoldCoordinate(newLetItem, manifoldCoordinate);
    }

    public void addManifoldCoordinate(LetItemKey newLetItem, ManifoldCoordinateForQuery newManifoldCoordinate) {
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        this.letItemObjectMap.put(newLetItem, newManifoldCoordinate);
        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), newManifoldCoordinate, this);
        this.letItemPanelMap.put(newLetItem, newLetItemPanel);

        this.letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);

    }

    public void addConceptSpecification(LetItemKey newLetItem, ObservableConceptProxy newConceptSpecification) {
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        this.letItemObjectMap.put(newLetItem, newConceptSpecification);
        LetItemPanel newLetItemPanel = new LetItemPanel(this.manifoldForDisplay, newLetItem,
                this.letItemsController.getLetListViewletListView(),
                new ObservableConceptProxy(this, TermAux.CONCEPT_FIELD.toExternalString(), new ConceptProxy(newConceptSpecification.get().getNid())), this);
        this.letItemPanelMap.put(newLetItem, newLetItemPanel);

        this.letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
        newConceptSpecification.addListener((observable, oldValue, newValue) -> {
            this.letItemObjectMap.put(newLetItem, newValue);
        });
        this.letItemObjectMap.addListener((MapChangeListener.Change<? extends LetItemKey, ? extends Object> change) -> {
            LetItemKey key = change.getKey();
            if (key.equals(newLetItem)) {
                if (change.wasAdded()) {
                    Object obj = change.getValueAdded();
                    if (obj instanceof ConceptProxy) {
                        newConceptSpecification.setValue((ConceptProxy) change.getValueAdded());
                    } else if (obj instanceof ObservableConceptProxy) {
                        ObservableConceptProxy proxy = (ObservableConceptProxy) obj;
                        newConceptSpecification.setValue(new ConceptProxy());
                    }

                }
            }
        });

    }

    public void addStampCoordinate(ActionEvent action) {

        LetItemKey newLetItem = new LetItemKey(createUniqueKey("STAMP"));
        ObservableStampCoordinate stampCoordinate = this.manifoldForDisplay.getStampCoordinate().deepClone();
        addStampCoordinate(newLetItem, stampCoordinate);
    }

    public void addConceptSpecification(ActionEvent action) {
        LetItemKey newLetItem = new LetItemKey(createUniqueKey("Concept"));
        ObservableConceptProxy newObjectProxy = new ObservableConceptProxy(this, TermAux.CONCEPT_FIELD.toExternalString(), 
                new ConceptProxy(TermAux.UNINITIALIZED_COMPONENT_ID));
        addConceptSpecification(newLetItem, newObjectProxy);
        
    }

    public void addLogicCoordinate(ActionEvent action) {
        String keyName = createUniqueKey("Logic");

        LetItemKey newLetItem = new LetItemKey(keyName);
        ObservableLogicCoordinate logicCoordinate = this.manifoldForDisplay.getLogicCoordinate().deepClone();
        addLogicCoordinate(newLetItem, logicCoordinate);
    }

    protected String createUniqueKey(String prefix) {
        int sequence = 0;
        String keyName = prefix;
        boolean unique = false;
        TRY_NEXT:
        while (!unique) {
            if (sequence > 0) {
                if (!prefix.endsWith(" ")) {
                    prefix = prefix + " ";
                }
                keyName = prefix + sequence;
            }
            sequence++;
            
            for (LetItemKey key : this.letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }
        return keyName;
    }

    public void addLogicCoordinate(LetItemKey newLetItem, LogicCoordinate newLogicCoordinate) {
        ObservableLogicCoordinate logicCoordinate;
        if (newLogicCoordinate instanceof ObservableStampCoordinate) {
            logicCoordinate = (ObservableLogicCoordinate) newLogicCoordinate;
        } else {
            logicCoordinate = new ObservableLogicCoordinateImpl(newLogicCoordinate);
        }
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        this.letItemObjectMap.put(newLetItem, logicCoordinate);
        LetItemPanel newLetItemPanel = new LetItemPanel(this.manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), logicCoordinate, this);
        this.letItemPanelMap.put(newLetItem, newLetItemPanel);

        this.letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);

    }

    public void addStampCoordinate(LetItemKey newLetItem, StampCoordinate newStampCoordinate) {
        ObservableStampCoordinate stampCoordinate;
        if (newStampCoordinate instanceof ObservableStampCoordinate) {
            stampCoordinate = (ObservableStampCoordinate) newStampCoordinate;
        } else {
            stampCoordinate = new ObservableStampCoordinateImpl(newStampCoordinate);
        }
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        this.letItemObjectMap.put(newLetItem, stampCoordinate);

        LetItemPanel newLetItemPanel = new LetItemPanel(this.manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), stampCoordinate, this);
        this.letItemPanelMap.put(newLetItem, newLetItemPanel);

        this.letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }

    public ObservableMap<LetItemKey, Object> getLetItemObjectMap() {
        return this.letItemObjectMap;
    }

    public Node getNode() {
        return this.propertySheetBorderPane;
    }

    public void setLetItemsController(LetItemsController letItemsController) {
        this.propertySheetBorderPane.setCenter(letItemsController.getNode());
        this.letItemsController = letItemsController;
        this.letItemsController.getLetListViewletListView().getSelectionModel().getSelectedIndices().addListener(this::handleSelectionChange);
    }

    private void handleSelectionChange(ListChangeListener.Change<? extends Integer> c) {
        if (c.getList().isEmpty()) {
            this.letItemsController.getLetItemBorderPane().setCenter(null);
            this.removeLetClause.setDisable(true);
        } else {
            this.removeLetClause.setDisable(false);
            LetItemKey selectedLetItem = this.letItemsController.getLetListViewletListView().getItems().get(c.getList().get(0));
            Node letNode = this.letItemPanelMap.get(selectedLetItem).getNode();
            if (letNode != this.letItemsController.getLetItemBorderPane().getCenter()) {
                this.letItemsController.getLetItemBorderPane().setCenter(letNode);
            }

        }
    }
}
