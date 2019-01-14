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
    private final ToolBar letToolbar = new ToolBar(addLetClauseButton);

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
        items = FXCollections.observableArrayList();
        MenuItem addStampCoordinate = new MenuItem("Add stamp coordinate");
        addStampCoordinate.setOnAction(this::addStampCoordinate);
        addLetClauseButton.getItems().add(addStampCoordinate);

        MenuItem addLanguageCoordinate = new MenuItem("Add language coordinate");
        addLanguageCoordinate.setOnAction(this::addLanguageCoordinate);
        addLetClauseButton.getItems().add(addLanguageCoordinate);

        MenuItem addLogicCoordinate = new MenuItem("Add logic coordinate");
        addLogicCoordinate.setOnAction(this::addLogicCoordinate);
        addLetClauseButton.getItems().add(addLogicCoordinate);

        MenuItem addManifoldCoordinate = new MenuItem("Add manifold coordinate");
        addManifoldCoordinate.setOnAction(this::addManifoldCoordinate);
        addLetClauseButton.getItems().add(addManifoldCoordinate);

        AnchorPane.setBottomAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setTopAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setLeftAnchor(this.propertySheetBorderPane, 0.0);
        AnchorPane.setRightAnchor(this.propertySheetBorderPane, 0.0);

        setupLetItemSubLists();  
        letItemObjectMap.addListener(this::letItemsChanged);
    }

    public ObservableList<LetItemKey> getStampCoordinateKeys() {
        return stampCoordinateKeys;
    }

    public ObservableList<LetItemKey> getLanguageCoordinateKeys() {
        return languageCoordinateKeys;
    }

    public ObservableList<LetItemKey> getLogicCoordinateKeys() {
        return logicCoordinateKeys;
    }

    public ObservableList<LetItemKey> getConceptSpecificationKeys() {
        return conceptSpecificationKeys;
    }

    public ObservableList<LetItemKey> getManifoldCoordinateKeys() {
        return manifoldCoordinateKeys;
    }

    public ObservableList<LetItemKey> getStringKeys() {
        return stringKeys;
    }

    private void setupLetItemSubLists() {
        stampCoordinateKeys.clear();
        languageCoordinateKeys.clear();
        logicCoordinateKeys.clear();
        manifoldCoordinateKeys.clear();
        conceptSpecificationKeys.clear();
        for (Map.Entry<LetItemKey, Object> entry: letItemObjectMap.entrySet()) {
            if (entry.getValue() instanceof StampCoordinate
                    && !(entry.getValue() instanceof ManifoldCoordinate)) {
                if (!stampCoordinateKeys.contains(entry.getKey())) {
                    stampCoordinateKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof LanguageCoordinate
                    && !(entry.getValue() instanceof ManifoldCoordinate)) {
                if (!languageCoordinateKeys.contains(entry.getKey())) {
                    languageCoordinateKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof LogicCoordinate
                    && !(entry.getValue() instanceof ManifoldCoordinate)) {
                if (!logicCoordinateKeys.contains(entry.getKey())) {
                    logicCoordinateKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof ManifoldCoordinate) {
                if (!manifoldCoordinateKeys.contains(entry.getKey())) {
                    manifoldCoordinateKeys.add(entry.getKey());
                }
                if (entry.getValue() instanceof ManifoldCoordinateForQuery) {
                    ((ManifoldCoordinateForQuery) entry.getValue()).setQuery(fLWORQueryController.getQuery());
                }
            }
            if (entry.getValue() instanceof ConceptSpecification) {
                if (!conceptSpecificationKeys.contains(entry.getKey())) {
                    conceptSpecificationKeys.add(entry.getKey());
                }
            }
            if (entry.getValue() instanceof String) {
                if (!stringKeys.contains(entry.getKey())) {
                    stringKeys.add(entry.getKey());
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
        letItemObjectMap.put(newLetItem, languageCoordinate);
        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), languageCoordinate, this);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }

    public void reset() {
        this.letItemPanelMap.clear();
        this.letItemObjectMap.clear();
        this.stampCoordinateKeys.clear();
        this.languageCoordinateKeys.clear();
        this.letItemsController.reset();
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
        } else {
            letItemObjectMap.put(newLetItem, newObject);
            FxGet.dialogs().showInformationDialog("Unsupported let item", "Can't create panel for " + newLetItem + ": " + newObject);
        }
    }

    public void addString(ActionEvent action) {
        int sequence = 1;
        String keyName = null;
        boolean unique = false;
        TRY_NEXT:
        while (!unique) {
            if (sequence > 1) {
                keyName = "String " + sequence++;
            } else {
                keyName = "String";
                sequence++;
            }
            for (LetItemKey key : letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }
        LetItemKey newLetItem = new LetItemKey(keyName);
        addString(newLetItem, "edit-me");
    }

    public void addString(LetItemKey newLetItem, String string) {
        SimpleStringProperty stringProperty = new SimpleStringProperty(this, MetaData.STRING____SOLOR.toExternalString(), string);

        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        letItemObjectMap.put(newLetItem, string);
        stringProperty.addListener((observable, oldValue, newValue) -> {
            letItemObjectMap.put(newLetItem, string);
        });
        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), stringProperty, this);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }

    public void addLanguageCoordinate(ActionEvent action) {
        int sequence = 1;
        String keyName = null;
        boolean unique = false;
        TRY_NEXT:
        while (!unique) {
            if (sequence > 1) {
                keyName = "[US, UK] English " + sequence++;
            } else {
                keyName = "[US, UK] English";
                sequence++;
            }
            for (LetItemKey key : letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }
        LetItemKey newLetItem = new LetItemKey(keyName);
        addLanguageCoordinate(newLetItem, this.manifoldForDisplay.getLanguageCoordinate().deepClone());
    }

    public void addManifoldCoordinate(ActionEvent action) {
        int sequence = 1;
        String keyName = "Manifold " + sequence;
        boolean unique = false;
        TRY_NEXT:
        while (!unique) {
            keyName = "Manifold " + sequence++;
            for (LetItemKey key : letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }

        LetItemKey newLetItem = new LetItemKey(keyName);

        ManifoldCoordinateForQuery manifoldCoordinate = new ManifoldCoordinateForQuery();
        if (!stampCoordinateKeys.isEmpty()) {
            manifoldCoordinate.setStampCoordinateKey(stampCoordinateKeys.get(0));
        }
        if (!languageCoordinateKeys.isEmpty()) {
            manifoldCoordinate.setLanguageCoordinateKey(languageCoordinateKeys.get(0));
        }
        if (!logicCoordinateKeys.isEmpty()) {
            manifoldCoordinate.setLogicCoordinateKey(logicCoordinateKeys.get(0));
        }
        manifoldCoordinate.setQuery(fLWORQueryController.getQuery());
        addManifoldCoordinate(newLetItem, manifoldCoordinate);
    }

    public void addManifoldCoordinate(LetItemKey newLetItem, ManifoldCoordinateForQuery newManifoldCoordinate) {
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        letItemObjectMap.put(newLetItem, newManifoldCoordinate);
        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), newManifoldCoordinate, this);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);

    }

    public void addConceptSpecification(LetItemKey newLetItem, ObservableConceptProxy newConceptSpecification) {
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        letItemObjectMap.put(newLetItem, newConceptSpecification);
        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem,
                this.letItemsController.getLetListViewletListView(),
                new ObservableConceptProxy(this, TermAux.CONCEPT_FIELD.toExternalString(), new ConceptProxy(newConceptSpecification)), this);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
        newConceptSpecification.addListener((observable, oldValue, newValue) -> {
            letItemObjectMap.put(newLetItem, newValue);
        });
        letItemObjectMap.addListener((MapChangeListener.Change<? extends LetItemKey, ? extends Object> change) -> {
            LetItemKey key = change.getKey();
            if (key.equals(newLetItem)) {
                if (change.wasRemoved() & !change.wasAdded()) {
                    newConceptSpecification.setValue(null);
                }
                if (change.wasAdded()) {
                    newConceptSpecification.setValue((ConceptProxy) change.getValueAdded());
                }
            }
        });

    }

    public void addStampCoordinate(ActionEvent action) {
        int sequence = 1;
        String keyName = "STAMP " + sequence;
        boolean unique = false;
        TRY_NEXT:
        while (!unique) {
            keyName = "STAMP " + sequence++;
            for (LetItemKey key : letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }

        LetItemKey newLetItem = new LetItemKey(keyName);
        ObservableStampCoordinate stampCoordinate = this.manifoldForDisplay.getStampCoordinate().deepClone();
        addStampCoordinate(newLetItem, stampCoordinate);
    }

    public void addLogicCoordinate(ActionEvent action) {
        int sequence = 1;
        String keyName = "Logic " + sequence;
        boolean unique = false;
        TRY_NEXT:
        while (!unique) {
            keyName = "Logic " + sequence++;
            for (LetItemKey key : letItemObjectMap.keySet()) {
                if (key.getItemName().equalsIgnoreCase(keyName)) {
                    continue TRY_NEXT;
                }
            }
            unique = true;
        }

        LetItemKey newLetItem = new LetItemKey(keyName);
        ObservableLogicCoordinate logicCoordinate = this.manifoldForDisplay.getLogicCoordinate().deepClone();
        addLogicCoordinate(newLetItem, logicCoordinate);
    }

    public void addLogicCoordinate(LetItemKey newLetItem, LogicCoordinate newLogicCoordinate) {
        ObservableLogicCoordinate logicCoordinate;
        if (newLogicCoordinate instanceof ObservableStampCoordinate) {
            logicCoordinate = (ObservableLogicCoordinate) newLogicCoordinate;
        } else {
            logicCoordinate = new ObservableLogicCoordinateImpl(newLogicCoordinate);
        }
        this.letItemsController.getLetListViewletListView().getItems().add(newLetItem);
        letItemObjectMap.put(newLetItem, logicCoordinate);
        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), logicCoordinate, this);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

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
        letItemObjectMap.put(newLetItem, stampCoordinate);

        LetItemPanel newLetItemPanel = new LetItemPanel(manifoldForDisplay, newLetItem, this.letItemsController.getLetListViewletListView(), stampCoordinate, this);
        letItemPanelMap.put(newLetItem, newLetItemPanel);

        letItemsController.getLetItemBorderPane().setCenter(newLetItemPanel.getNode());

        this.letItemsController.getLetListViewletListView().getSelectionModel().select(newLetItem);
    }

    public ObservableMap<LetItemKey, Object> getLetItemObjectMap() {
        return letItemObjectMap;
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
            letItemsController.getLetItemBorderPane().setCenter(null);
        } else {
            LetItemKey selectedLetItem = this.letItemsController.getLetListViewletListView().getItems().get(c.getList().get(0));
            Node letNode = letItemPanelMap.get(selectedLetItem).getNode();
            if (letNode != letItemsController.getLetItemBorderPane().getCenter()) {
                letItemsController.getLetItemBorderPane().setCenter(letNode);
            }

        }
    }
}
