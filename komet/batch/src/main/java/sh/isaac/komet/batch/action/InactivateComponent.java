package sh.isaac.komet.batch.action;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.ViewProperties;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InactivateComponent extends ActionItem {

    public static final int marshalVersion = 1;
    public static final String INACTIVATE_COMPONENT = "Inactivate component";
    // concept to find
    SimpleObjectProperty<ConceptSpecification> conceptToInactivateProperty = new SimpleObjectProperty<>(this,
            MetaData.CONCEPT_TO_FIND____SOLOR.toExternalString() , MetaData.UNINITIALIZED_COMPONENT____SOLOR);

    public InactivateComponent(ByteArrayDataBuffer in) {
        this.conceptToInactivateProperty.set(in.getConceptSpecification());
    }

    public InactivateComponent() {
    }

    @Override
    protected void setupItemForGui(ManifoldCoordinate manifoldForDisplay) {
        getPropertySheet().getItems().add(new PropertySheetItemConceptWrapper(manifoldForDisplay, "Inactivate",
                conceptToInactivateProperty,
                new ConceptProxy("Disease (disorder)", UUID.fromString("ab4e618b-b954-3d56-a44b-f0f29d6f59d3")),
                MetaData.FINDING____SOLOR));

    }

    @Override
    protected void setupForApply(ConcurrentHashMap<Enum, Object> cache, Transaction transaction, StampFilter stampFilter, EditCoordinate editCoordinate) {
        Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getChronology(conceptToInactivateProperty.get().getNid());
        if (optionalChronology.isPresent()) {
            Chronology chronology = optionalChronology.get();
            chronology.createMutableVersion(transaction, Status.INACTIVE, editCoordinate);
            Get.identifiedObjectService().putChronologyData(chronology);
        }
    }

    @Override
    protected void apply(Chronology chronology, ConcurrentHashMap<Enum, Object> cache, Transaction transaction, StampFilter stampFilter, EditCoordinate editCoordinate, VersionChangeListener versionChangeListener) {
        // nothing to do..
    }

    @Override
    public String getTitle() {
        return INACTIVATE_COMPONENT;
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putConceptSpecification(conceptToInactivateProperty.get());
    }

    @Unmarshaler
    public static Object make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new InactivateComponent(in);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

}
