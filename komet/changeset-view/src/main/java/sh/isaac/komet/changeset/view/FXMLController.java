package sh.isaac.komet.changeset.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;

public class FXMLController {

    protected static final Logger LOG = LogManager.getLogger();

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="changeSetTreeTable"
    private TreeTableView<?> changeSetTreeTable; // Value injected by FXMLLoader

    @FXML // fx:id="typeColumn"
    private TreeTableColumn typeColumn; // Value injected by FXMLLoader

    @FXML // fx:id="infoColumn"
    private TreeTableColumn infoColumn; // Value injected by FXMLLoader

    @FXML // fx:id="statusColumn"
    private TreeTableColumn statusColumn; // Value injected by FXMLLoader

    @FXML // fx:id="timeColumn"
    private TreeTableColumn timeColumn; // Value injected by FXMLLoader

    @FXML // fx:id="authorColumn"
    private TreeTableColumn authorColumn; // Value injected by FXMLLoader

    @FXML // fx:id="moduleColumn"
    private TreeTableColumn moduleColumn; // Value injected by FXMLLoader

    @FXML // fx:id="pathColumn"
    private TreeTableColumn pathColumn; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert changeSetTreeTable != null : "fx:id=\"changeSetTreeTable\" was not injected: check your FXML file 'Scene.fxml'.";
        assert typeColumn != null : "fx:id=\"typeColumn\" was not injected: check your FXML file 'Scene.fxml'.";
        assert infoColumn != null : "fx:id=\"infoColumn\" was not injected: check your FXML file 'Scene.fxml'.";
        assert statusColumn != null : "fx:id=\"statusColumn\" was not injected: check your FXML file 'Scene.fxml'.";
        assert timeColumn != null : "fx:id=\"timeColumn\" was not injected: check your FXML file 'Scene.fxml'.";
        assert authorColumn != null : "fx:id=\"authorColumn\" was not injected: check your FXML file 'Scene.fxml'.";
        assert moduleColumn != null : "fx:id=\"moduleColumn\" was not injected: check your FXML file 'Scene.fxml'.";
        assert pathColumn != null : "fx:id=\"pathColumn\" was not injected: check your FXML file 'Scene.fxml'.";
        this.statusColumn.setCellValueFactory(new StatusCallbackImpl());
        this.timeColumn.setCellValueFactory(new TimeCallbackImpl());
        this.authorColumn.setCellValueFactory(new AuthorCallbackImpl());
        this.moduleColumn.setCellValueFactory(new ModuleCallbackImpl());
        this.pathColumn.setCellValueFactory(new PathCallbackImpl());
        this.typeColumn.setCellValueFactory(new TypeCallbackImpl());
        this.infoColumn.setCellValueFactory(new InfoCallbackImpl());

    }

    public void setFile(File changeSetFile) {
        try {
            BinaryDataReaderService reader = Get.binaryDataReader(changeSetFile.toPath());
            Stream<IsaacExternalizable> externalizableStream = reader.getStream();
            ObservableList<IsaacExternalizable> itemList = FXCollections.observableArrayList();
            TreeItem root = new TreeItem("root");
            changeSetTreeTable.setRoot(root);
            changeSetTreeTable.showRootProperty().set(false);
            externalizableStream.forEach(item -> processItem(root, item));
            //typeColumn.setCellValueFactory(new PropertyValueFactory("isaacObjectType"));
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void processItem(TreeItem parent, Object item) {
        if (item instanceof IsaacExternalizable) {
            IsaacObjectType objectType = ((IsaacExternalizable) item).getIsaacObjectType();
            switch (objectType) {
                case CONCEPT: {
                    ConceptChronologyImpl conceptItem = (ConceptChronologyImpl) item;
                    TreeItem conceptTreeItem = new TreeItem(item);
                    conceptTreeItem.setExpanded(true);
                    parent.getChildren().add(conceptTreeItem);
                    for (Version version : conceptItem.getVersionList()) {
                        processItem(conceptTreeItem, version);
                    }
                    break;
                }
                case SEMANTIC: {
                    SemanticChronologyImpl semanticItem = (SemanticChronologyImpl) item;
                    TreeItem semanticTreeItem = new TreeItem(item);
                    semanticTreeItem.setExpanded(true);
                    parent.getChildren().add(semanticTreeItem);
                    for (Version version : semanticItem.getVersionList()) {
                        processItem(semanticTreeItem, version);
                    }
                    break;
                }
                case STAMP: {
                    parent.getChildren().add(new TreeItem(objectType));
                    break;
                }
                case STAMP_ALIAS: {
                    parent.getChildren().add(new TreeItem(objectType));
                    break;
                }
                case STAMP_COMMENT: {
                    parent.getChildren().add(new TreeItem(objectType));
                    break;
                }
                case UNKNOWN:
                default:
                    throw new UnsupportedOperationException("Can't handle: " + objectType);
            }
        } else if (item instanceof Version) {
            Version version = (Version) item; //SemanticVersionImpl, ConceptVersionImpl, DescriptionVersionImpl, ComponentNidVersionImpl, LogicGraphVersionImpl, StringVersionImpl
            TreeItem versionTreeItem = new TreeItem(item);
            parent.getChildren().add(versionTreeItem);
        }
    }

    private class TypeCallbackImpl implements Callback<TreeTableColumn.CellDataFeatures, ObservableValue> {

        @Override
        public ObservableValue<?> call(TreeTableColumn.CellDataFeatures cellData) {
            Object item = cellData.getValue().getValue();
            if (item instanceof IsaacExternalizable) {
                IsaacObjectType objectType = ((IsaacExternalizable) item).getIsaacObjectType();
                if (objectType == IsaacObjectType.SEMANTIC) {
                    SemanticChronologyImpl semanticItem = (SemanticChronologyImpl) item;
                    return new ReadOnlyObjectWrapper(semanticItem.getVersionType());
                }
                return new ReadOnlyObjectWrapper(objectType);
            } else if (item instanceof Version) {
                Version version = (Version) item;
                return new ReadOnlyObjectWrapper("version");
            }
            return new ReadOnlyObjectWrapper(item.getClass().getName());
        }
    }

    private class StatusCallbackImpl implements Callback<TreeTableColumn.CellDataFeatures, ObservableValue> {

        @Override
        public ObservableValue<?> call(TreeTableColumn.CellDataFeatures cellData) {
            Object item = cellData.getValue().getValue();
            if (item instanceof IsaacExternalizable) {
                return null;
            } else if (item instanceof Version) {
                Version version = (Version) item;
                return new ReadOnlyObjectWrapper(version.getStatus());
            }
            return new ReadOnlyObjectWrapper(item.getClass().getName());
        }
    }
    private class TimeCallbackImpl implements Callback<TreeTableColumn.CellDataFeatures, ObservableValue> {

        @Override
        public ObservableValue<?> call(TreeTableColumn.CellDataFeatures cellData) {
            Object item = cellData.getValue().getValue();
            if (item instanceof IsaacExternalizable) {
                return null;
            } else if (item instanceof Version) {
                Version version = (Version) item;
                
                return new ReadOnlyObjectWrapper(DateTimeUtil.format(version.getTime()));
            }
            return new ReadOnlyObjectWrapper(item.getClass().getName());
        }
    }
    private class AuthorCallbackImpl implements Callback<TreeTableColumn.CellDataFeatures, ObservableValue> {
        @Override
        public ObservableValue<?> call(TreeTableColumn.CellDataFeatures cellData) {
            Object item = cellData.getValue().getValue();
            if (item instanceof IsaacExternalizable) {
                return null;
            } else if (item instanceof Version) {
                Version version = (Version) item;
                return new ReadOnlyObjectWrapper(Get.defaultCoordinate().getPreferredDescriptionText(version.getAuthorNid()));
            }
            return new ReadOnlyObjectWrapper(item.getClass().getName());
        }
    }
    
    private class ModuleCallbackImpl implements Callback<TreeTableColumn.CellDataFeatures, ObservableValue> {
        @Override
        public ObservableValue<?> call(TreeTableColumn.CellDataFeatures cellData) {
            Object item = cellData.getValue().getValue();
            if (item instanceof IsaacExternalizable) {
                return null;
            } else if (item instanceof Version) {
                Version version = (Version) item;
                return new ReadOnlyObjectWrapper(Get.defaultCoordinate().getPreferredDescriptionText(version.getModuleNid()));
            }
            return new ReadOnlyObjectWrapper(item.getClass().getName());
        }
    }
    private class PathCallbackImpl implements Callback<TreeTableColumn.CellDataFeatures, ObservableValue> {
        @Override
        public ObservableValue<?> call(TreeTableColumn.CellDataFeatures cellData) {
            Object item = cellData.getValue().getValue();
            if (item instanceof IsaacExternalizable) {
                return null;
            } else if (item instanceof Version) {
                Version version = (Version) item;
                return new ReadOnlyObjectWrapper(Get.defaultCoordinate().getPreferredDescriptionText(version.getPathNid()));
            }
            return new ReadOnlyObjectWrapper(item.getClass().getName());
        }
    }

    private class InfoCallbackImpl implements Callback<TreeTableColumn.CellDataFeatures, ObservableValue> {
        @Override
        public ObservableValue<?> call(TreeTableColumn.CellDataFeatures cellData) {
            Object item = cellData.getValue().getValue();
            if (item instanceof IsaacExternalizable) {
                IsaacObjectType objectType = ((IsaacExternalizable) item).getIsaacObjectType();
                if (objectType == IsaacObjectType.SEMANTIC) {
                    SemanticChronologyImpl semanticItem = (SemanticChronologyImpl) item;
                    return new ReadOnlyObjectWrapper("id: " + semanticItem.getPrimordialUuid().toString() + "\nrc: " +
                            Get.identifierService().getUuidPrimordialForNid(semanticItem.getReferencedComponentNid()).toString()
                    + "\n     " + Get.defaultCoordinate().getPreferredDescriptionText(semanticItem.getAssemblageNid()));
                } else if (objectType == IsaacObjectType.CONCEPT) {
                    ConceptChronologyImpl concept = (ConceptChronologyImpl) item;
                    return new ReadOnlyObjectWrapper("id: " + concept.getPrimordialUuid().toString() 
                            + "\n     " + Get.defaultCoordinate().getPreferredDescriptionText(concept.getAssemblageNid()));
                }
                return new ReadOnlyObjectWrapper(objectType);
            } else if (item instanceof Version) {
                Version version = (Version) item;
                switch (version.getSemanticType()) {
                    case DESCRIPTION: {
                        DescriptionVersion descriptionVersion = (DescriptionVersion) version;
                        return new ReadOnlyObjectWrapper(descriptionVersion.getText());
                    }
                    case COMPONENT_NID: {
                        ComponentNidVersion typedVersion = (ComponentNidVersion) version;
                        return new ReadOnlyObjectWrapper(Get.defaultCoordinate().getPreferredDescriptionText(typedVersion.getComponentNid()));
                    }
                    case LOGIC_GRAPH:{
                        LogicGraphVersion typedVersion = (LogicGraphVersion) version;
                        return new ReadOnlyObjectWrapper(typedVersion.getLogicalExpression().toString());
                    }
                    case STRING: {
                        StringVersion typedVersion = (StringVersion) version;
                        return new ReadOnlyObjectWrapper(typedVersion.getString());
                    }
                    case CONCEPT: {
                        return null;
                    }
                    case DYNAMIC:
                    case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                    case LONG:
                    case MEASURE_CONSTRAINTS:
                    case MEMBER:
                    case Nid1_Int2:
                    case Nid1_Long2:
                    case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                    case Nid1_Nid2:
                    case Nid1_Nid2_Int3:
                    case Nid1_Nid2_Str3:
                    case Nid1_Str2:
                    case RF2_RELATIONSHIP:
                    case Str1_Nid2_Nid3_Nid4:
                    case Str1_Str2:
                    case Str1_Str2_Nid3_Nid4:
                    case Str1_Str2_Nid3_Nid4_Nid5:
                    case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                    case UNKNOWN:
                }
                return new ReadOnlyObjectWrapper(version.toUserString());
            }
            return new ReadOnlyObjectWrapper(item.getClass().getName());
        }
    }
    
}
