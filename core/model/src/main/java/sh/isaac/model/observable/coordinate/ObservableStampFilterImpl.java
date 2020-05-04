package sh.isaac.model.observable.coordinate;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.SimpleEqualityBasedSetProperty;

import java.util.Collection;

public class ObservableStampFilterImpl extends ObservableCoordinateImpl<StampFilterImmutable> implements ObservableStampFilter {

    private final LongProperty timeProperty;

    /**
     *
     * @return the property that identifies the path concept for this path coordinate
     */
    private final ObjectProperty<ConceptSpecification> pathConceptProperty;

    private final ObservableStampFilterTemplateImpl observableStampFilterTemplate;

    private ObservableStampFilterImpl(StampPositionImmutable stampPositionImmutable, ObservableStampFilterTemplateImpl observableStampFilterTemplate) {
        this(StampFilterImmutable.make(observableStampFilterTemplate.getAllowedStates(),
                stampPositionImmutable, observableStampFilterTemplate.getModuleNids(), observableStampFilterTemplate.getModulePriorityOrder()),
                observableStampFilterTemplate);
    }

    private ObservableStampFilterImpl(StampFilterImmutable stampFilterImmutable, ObservableStampFilterTemplateImpl observableStampFilterTemplate) {
        super(stampFilterImmutable);

        this.observableStampFilterTemplate = observableStampFilterTemplate;

        this.pathConceptProperty = new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
                stampFilterImmutable.getPathConceptForFilter());

        this.timeProperty = new SimpleLongProperty(this,
                ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
                stampFilterImmutable.getStampPosition().getTime());

        addListeners();

    }


    private ObservableStampFilterImpl(StampFilterImmutable stampFilterImmutable) {
        super(stampFilterImmutable);

        this.observableStampFilterTemplate = ObservableStampFilterTemplateImpl.make(stampFilterImmutable.toStampFilterTemplateImmutable());

        this.pathConceptProperty = new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
                stampFilterImmutable.getPathConceptForFilter());

        this.timeProperty = new SimpleLongProperty(this,
                ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
                stampFilterImmutable.getStampPosition().getTime());

        addListeners();

    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampFilterImmutable> observable, StampFilterImmutable oldValue, StampFilterImmutable newValue) {
        this.observableStampFilterTemplate.setValue(newValue.toStampFilterTemplateImmutable());
        this.pathConceptProperty.setValue(newValue.getPathConceptForFilter());
        this.timeProperty.set(newValue.getStampPosition().getTime());
    }

    @Override
    protected void addListeners() {
        this.pathConceptProperty.addListener(this::pathConceptChanged);
        this.timeProperty.addListener(this::timeChanged);
    }

    @Override
    protected void removeListeners() {
        this.pathConceptProperty.removeListener(this::pathConceptChanged);
        this.timeProperty.removeListener(this::timeChanged);
    }

    public static ObservableStampFilterImpl make(StampFilter stampFilter) {
        return new ObservableStampFilterImpl(stampFilter.toStampFilterImmutable());
    }

    private void timeChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newTime) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(newTime.longValue(), getPathNidForFilter()),
                getModuleNids(),
                getModulePriorityOrder()));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observablePathConcept,
                                    ConceptSpecification oldPathConcept,
                                    ConceptSpecification newPathConcept) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(timeProperty.longValue(), newPathConcept.getNid()),
                getModuleNids(),
                getModulePriorityOrder()));
    }

    @Override
    public LongProperty timeProperty() {
        return this.timeProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> pathConceptProperty() {
        return this.pathConceptProperty;
    }

    @Override
    public SetProperty<ConceptSpecification> moduleSpecificationsProperty() {
        return this.observableStampFilterTemplate.moduleSpecificationsProperty();
    }

    /**
     *
     * @return the specified modules property
     */
    public SetProperty<ConceptSpecification> excludedModuleSpecificationsProperty() {
        return this.observableStampFilterTemplate.excludedModuleSpecificationsProperty();
    }


    @Override
    public ListProperty<ConceptSpecification> modulePriorityOrderProperty() {
        return this.observableStampFilterTemplate.modulePriorityOrderProperty();
    }

    /**
     * Allowed states property.
     *
     * @return the set property
     */
    @Override
    public SetProperty<Status> allowedStatusProperty() {
        return this.observableStampFilterTemplate.allowedStatusProperty();
    }

    @Override
    public StampFilter getStampFilter() {
        return getValue();
    }

    @Override
    public ObservableStampFilter makeModuleAnalog(Collection<ConceptSpecification> modules) {
        return new ObservableStampFilterImpl(getValue().makeModuleAnalog(modules));
    }

    @Override
    public ObservableStampFilter makePathAnalog(ConceptSpecification pathForPosition) {
        return new ObservableStampFilterImpl(getValue().makePathAnalog(pathForPosition));
    }

    @Override
    public StampFilterTemplate getStampFilterTemplate() {
        return this.observableStampFilterTemplate.getValue();
    }

    @Override
    public StampFilterTemplateImmutable toStampFilterTemplateImmutable() {
        return this.observableStampFilterTemplate.getValue();
    }
}
