package sh.isaac.komet.preferences.coordinate;

import javafx.beans.property.SimpleStringProperty;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.isaac.model.coordinate.LogicCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

public class LogicCoordinateItemPanel extends AbstractPreferences {

    public enum Keys {
        LOGIC__COORDINATE_DATA
    }

    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.LOGIC_COORDINATE_NAME____SOLOR.toExternalString());

    private final ObservableLogicCoordinateImpl logicCoordinateItem;

    private final UuidStringKey itemKey;

    public LogicCoordinateItemPanel(LogicCoordinate logicCoordinate, String coordinateName,
                                    IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {

        super(preferencesNode, coordinateName, manifold, kpc);
        if (logicCoordinate instanceof ManifoldCoordinate) {
            logicCoordinate = ((ManifoldCoordinate) logicCoordinate).getLogicCoordinate();
        }
        if (logicCoordinate instanceof ObservableLogicCoordinate) {
            logicCoordinate = ((ObservableLogicCoordinate) logicCoordinate).getLogicCoordinate();
        }
        this.logicCoordinateItem = new ObservableLogicCoordinateImpl(logicCoordinate.deepClone());

        setup(manifold);
        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.logicCoordinates().put(itemKey, logicCoordinateItem);
    }

    public LogicCoordinateItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME).get(), manifold, kpc);
        Optional<byte[]> optionalBytes = preferencesNode.getByteArray(Keys.LOGIC__COORDINATE_DATA);
        if (optionalBytes.isPresent()) {
            ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(optionalBytes.get());
            LogicCoordinateImpl logicCoordinate = LogicCoordinateImpl.make(buffer);
            this.logicCoordinateItem = new ObservableLogicCoordinateImpl(logicCoordinate);
        } else {
            setGroupName("US English");
            LogicCoordinate logicCoordinate = Get.coordinateFactory().createStandardElProfileLogicCoordinate();
            this.logicCoordinateItem = new ObservableLogicCoordinateImpl(logicCoordinate.deepClone());
        }
        setup(manifold);
        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.logicCoordinates().put(itemKey, logicCoordinateItem);
    }


    private void setup(Manifold manifold) {

        nameProperty.set(groupNameProperty().get());
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.languageCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            FxGet.logicCoordinates().put(itemKey, logicCoordinateItem);
        });

        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Logic profile", logicCoordinateItem.descriptionLogicProfileProperty(),
                new ConceptSpecification[] { TermAux.EL_PLUS_PLUS_LOGIC_PROFILE }));
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Classifier", logicCoordinateItem.classifierProperty(),
                new ConceptSpecification[] { TermAux.SNOROCKET_CLASSIFIER }));
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Concepts to classify", logicCoordinateItem.conceptAssemblageProperty(),
                new ConceptSpecification[] { TermAux.SOLOR_CONCEPT_ASSEMBLAGE }));
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Stated assemblage", logicCoordinateItem.statedAssemblageProperty(),
                new ConceptSpecification[] { TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE }));
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Inferred assemblage", logicCoordinateItem.inferredAssemblageProperty(),
                new ConceptSpecification[] { TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE }));
        revert();
        save();
    }


    @Override
    protected void saveFields() throws BackingStoreException {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        ByteArrayDataBuffer buff = new ByteArrayDataBuffer();
        this.logicCoordinateItem.putExternal(buff);
        buff.trimToSize();
        getPreferencesNode().putByteArray(Keys.LOGIC__COORDINATE_DATA, buff.getData());
   }

    @Override
    protected void revertFields() throws BackingStoreException {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        ByteArrayDataBuffer revertbuffer = new ByteArrayDataBuffer();
        this.logicCoordinateItem.putExternal(revertbuffer);
        revertbuffer.trimToSize();
        byte[] data = getPreferencesNode().getByteArray(Keys.LOGIC__COORDINATE_DATA, revertbuffer.getData());
        ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(data);
        LogicCoordinateImpl logicCoordinate = LogicCoordinateImpl.make(buffer);
        if (!logicCoordinate.getLogicCoordinateUuid().equals(this.logicCoordinateItem.getLogicCoordinateUuid())) {
            this.logicCoordinateItem.setClassifier(logicCoordinate.getClassifier());
            this.logicCoordinateItem.setConceptAssemblage(logicCoordinate.getConceptAssemblage());
            this.logicCoordinateItem.setDescriptionLogicProfile(logicCoordinate.getDescriptionLogicProfile());
            this.logicCoordinateItem.setInferredAssemblage(logicCoordinate.getInferredAssemblage());
            this.logicCoordinateItem.setStatedAssemblage(logicCoordinate.getStatedAssemblage());
        }
    }
}