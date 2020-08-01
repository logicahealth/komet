package sh.isaac.komet.batch.action;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReplaceAllInExpression extends ActionItem {
    public static final int marshalVersion = 1;

    private enum Keys {
        EXPRESSION_SNAPSHOT;
    }

    public static final String REPLACE_ALL_IN_EXPRESSION = "Replace all in referenced expression";

    // Expression assemblage
    SimpleObjectProperty<ConceptSpecification> expressionAssemblageProperty = new SimpleObjectProperty<>(this,
            MetaData.ASSEMBLAGE_FOR_ACTION____SOLOR.toExternalString() , TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE);

    // concept to find
    SimpleObjectProperty<ConceptSpecification> conceptToFindProperty = new SimpleObjectProperty<>(this,
            MetaData.CONCEPT_TO_FIND____SOLOR.toExternalString() , MetaData.FINDING____SOLOR);

    // replaceWith
    SimpleObjectProperty<ConceptSpecification> replaceWithProperty = new SimpleObjectProperty<>(this,
            MetaData.REPLACEMENT_CONCEPT____SOLOR.toExternalString(), MetaData.PHENOMENON____SOLOR);

    public ReplaceAllInExpression() {
    }

    public ReplaceAllInExpression(ByteArrayDataBuffer in) {
        expressionAssemblageProperty.set(in.getConceptSpecification());
        conceptToFindProperty.set(in.getConceptSpecification());
        replaceWithProperty.set(in.getConceptSpecification());
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putConceptSpecification(expressionAssemblageProperty.get());
        out.putConceptSpecification(conceptToFindProperty.get());
        out.putConceptSpecification(replaceWithProperty.get());
    }

    @Unmarshaler
    public static Object make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new ReplaceAllInExpression(in);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    public void setupItemForGui(ManifoldCoordinate manifoldForDisplay) {
        getPropertySheet().getItems().add(new PropertySheetItemConceptWrapper(manifoldForDisplay, "Search in",
                expressionAssemblageProperty, TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE));

        getPropertySheet().getItems().add(new PropertySheetItemConceptWrapper(manifoldForDisplay, "Find concept",
                conceptToFindProperty,
                new ConceptProxy("Disease (disorder)", UUID.fromString("ab4e618b-b954-3d56-a44b-f0f29d6f59d3")),
                MetaData.FINDING____SOLOR));

        getPropertySheet().getItems().add(new PropertySheetItemConceptWrapper(manifoldForDisplay, "Replace with",
                replaceWithProperty, MetaData.FINDING____SOLOR, MetaData.PHENOMENON____SOLOR));

    }

    @Override
    protected void setupForApply(ConcurrentHashMap<Enum, Object> cache, Transaction transaction, ManifoldCoordinate manifoldCoordinate) {
        // Setup snapshot...
        SingleAssemblageSnapshot<LogicGraphVersion> snapshot =
                Get.assemblageService().getSingleAssemblageSnapshot(expressionAssemblageProperty.get().getNid(),
                        LogicGraphVersion.class, manifoldCoordinate.getViewStampFilter());
        cache.put(Keys.EXPRESSION_SNAPSHOT, snapshot);
    }

    @Override
    protected void apply(Chronology chronology, ConcurrentHashMap<Enum, Object> cache, Transaction transaction,
                         ManifoldCoordinate manifoldCoordinate, VersionChangeListener versionChangeListener) {
        SingleAssemblageSnapshot<LogicGraphVersion> snapshot = (SingleAssemblageSnapshot<LogicGraphVersion>) cache.get(Keys.EXPRESSION_SNAPSHOT);
        List<LatestVersion<LogicGraphVersion>> latestVersionList = snapshot.getLatestSemanticVersionsForComponentFromAssemblage(chronology.getNid());
        for (LatestVersion<LogicGraphVersion> latestVersion: latestVersionList) {
            if (latestVersion.isPresent()) {
                LogicGraphVersion latest = latestVersion.get();
                LogicalExpression expression = latest.getLogicalExpression();
                if (expression.containsConcept(conceptToFindProperty.get())) {
                    LogicalExpression newExpression = expression.replaceAllConceptOccurences(conceptToFindProperty.get(),
                            replaceWithProperty.get());
                    LogicGraphVersionImpl mutableVersion = latest.getChronology().createMutableVersion(transaction, Status.ACTIVE, manifoldCoordinate);
                    mutableVersion.setLogicalExpression(newExpression);
                    versionChangeListener.versionChanged(latest, mutableVersion);
                    Get.identifiedObjectService().putChronologyData(mutableVersion.getChronology());
                }
            }
        }
    }

    @Override
    public String getTitle() {
        return REPLACE_ALL_IN_EXPRESSION;
    }

}
