package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

import java.util.ArrayList;
import java.util.Optional;

public abstract class ObservableLanguageCoordinateBase extends ObservableCoordinateImpl<LanguageCoordinateImmutable>
        implements ObservableLanguageCoordinate {

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ObservableLanguageCoordinate> nextPriorityLanguageCoordinateChangedListener = this::nextPriorityLanguageCoordinateChanged;
    private final ChangeListener<ConceptSpecification> languageConceptChangedListener = this::languageConceptChanged;
    private final ListChangeListener<ConceptSpecification> modulePreferenceListChangedListener = this::modulePreferenceListChanged;
    private final ListChangeListener<ConceptSpecification> descriptionTypePreferenceListListener = this::descriptionTypePreferenceListChanged;
    private final ListChangeListener<ConceptSpecification> dialectAssemblagePreferenceListChangedListener = this::dialectAssemblagePreferenceListChanged;


    /**
     * The language concept nid property.
     */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> languageProperty;

    /**
     * The dialect assemblage preference list property.
     */
    private final SimpleEqualityBasedListProperty<ConceptSpecification> dialectAssemblagePreferenceListProperty;

    /**
     * The description type preference list property.
     */
    private final SimpleEqualityBasedListProperty<ConceptSpecification> descriptionTypePreferenceListProperty;

    private final SimpleEqualityBasedListProperty<ConceptSpecification> modulePreferenceListProperty;

    private final SimpleEqualityBasedObjectProperty<ObservableLanguageCoordinate> nextPriorityLanguageCoordinateProperty;


    protected ObservableLanguageCoordinateBase(LanguageCoordinate languageCoordinate, String coordinateName) {
        super(languageCoordinate.toLanguageCoordinateImmutable(), coordinateName);
        this.languageProperty = makeLanguageProperty(languageCoordinate);
        this.dialectAssemblagePreferenceListProperty = makeDialectAssemblagePreferenceListProperty(languageCoordinate);
        this.descriptionTypePreferenceListProperty = makeDescriptionTypePreferenceListProperty(languageCoordinate);
        this.modulePreferenceListProperty = makeModulePreferenceListProperty(languageCoordinate);
        this.nextPriorityLanguageCoordinateProperty = makeNextPriorityLanguageCoordinateProperty(languageCoordinate);
        addListeners();
    }
    /**
     * The language concept nid property.
     */
    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makeLanguageProperty(LanguageCoordinate languageCoordinate);

    /**
     * The dialect assemblage preference list property.
     */
    protected abstract SimpleEqualityBasedListProperty<ConceptSpecification> makeDialectAssemblagePreferenceListProperty(LanguageCoordinate languageCoordinate);

    /**
     * The description type preference list property.
     */
    protected abstract SimpleEqualityBasedListProperty<ConceptSpecification> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate);

    protected abstract SimpleEqualityBasedListProperty<ConceptSpecification> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate);

    protected abstract SimpleEqualityBasedObjectProperty<ObservableLanguageCoordinate> makeNextPriorityLanguageCoordinateProperty(LanguageCoordinate languageCoordinate);

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends LanguageCoordinateImmutable> observable, LanguageCoordinateImmutable oldValue, LanguageCoordinateImmutable newValue) {
        this.languageProperty.setValue(newValue.getLanguageConcept());
        this.dialectAssemblagePreferenceListProperty.setAll(newValue.getDialectAssemblageSpecPreferenceList());
        this.descriptionTypePreferenceListProperty.setAll(newValue.getDescriptionTypeSpecPreferenceList());
        this.modulePreferenceListProperty.setAll(newValue.getModuleSpecPreferenceListForLanguage());
        if (newValue.getNextPriorityLanguageCoordinate().isPresent()) {
            if (this.nextPriorityLanguageCoordinateProperty.get() != null) {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                this.nextPriorityLanguageCoordinateProperty.get().setValue(languageCoordinateImmutable);
            } else {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                ObservableLanguageCoordinateImpl observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(languageCoordinateImmutable);
                this.nextPriorityLanguageCoordinateProperty.setValue(observableLanguageCoordinate);
            }
        } else {
            this.nextPriorityLanguageCoordinateProperty.setValue(null);
        }
    }

    @Override
    protected void addListeners() {
        this.languageConceptProperty().addListener(this.languageConceptChangedListener);
        this.dialectAssemblagePreferenceListProperty().addListener(this.dialectAssemblagePreferenceListChangedListener);
        this.descriptionTypePreferenceListProperty().addListener(this.descriptionTypePreferenceListListener);
        this.modulePreferenceListForLanguageProperty().addListener(this.modulePreferenceListChangedListener);
        this.nextPriorityLanguageCoordinateProperty().addListener(this.nextPriorityLanguageCoordinateChangedListener);
    }

    @Override
    protected void removeListeners() {
        this.languageConceptProperty().removeListener(this.languageConceptChangedListener);
        this.dialectAssemblagePreferenceListProperty().removeListener(this.dialectAssemblagePreferenceListChangedListener);
        this.descriptionTypePreferenceListProperty().removeListener(this.descriptionTypePreferenceListListener);
        this.modulePreferenceListForLanguageProperty().removeListener(this.modulePreferenceListChangedListener);
        this.nextPriorityLanguageCoordinateProperty().removeListener(this.nextPriorityLanguageCoordinateChangedListener);
    }

    private void nextPriorityLanguageCoordinateChanged(ObservableValue<? extends ObservableLanguageCoordinate> observable,
                                                      ObservableLanguageCoordinate oldNextPriorityCoordinate,
                                                      ObservableLanguageCoordinate newNextPriorityCoordinate) {
        if (newNextPriorityCoordinate == null) {
            this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                    IntLists.immutable.of(getDescriptionTypePreferenceList()),
                    IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                    IntLists.immutable.of(getModulePreferenceListForLanguage()),
                    Optional.empty()));
        } else {
            this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                    IntLists.immutable.of(getDescriptionTypePreferenceList()),
                    IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                    IntLists.immutable.of(getModulePreferenceListForLanguage()),
                    Optional.of(newNextPriorityCoordinate.getValue())));
        }
    }

    private void modulePreferenceListChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                IntLists.immutable.of(getDescriptionTypePreferenceList()),
                IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray()),
                getNextPriorityLanguageCoordinate()));
    }
    private void descriptionTypePreferenceListChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                IntLists.immutable.of(getModulePreferenceListForLanguage()),
                getNextPriorityLanguageCoordinate()));
    }

    private void dialectAssemblagePreferenceListChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                IntLists.immutable.of(getDescriptionTypePreferenceList()),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(getModulePreferenceListForLanguage()),
                getNextPriorityLanguageCoordinate()));
    }

    private void languageConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                        ConceptSpecification oldLanguageConcept,
                                        ConceptSpecification newLanguageConcept) {
        this.setValue(LanguageCoordinateImmutable.make(newLanguageConcept.getNid(),
                IntLists.immutable.of(getDescriptionTypePreferenceList()),
                IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                IntLists.immutable.of(getModulePreferenceListForLanguage()),
                getNextPriorityLanguageCoordinate()));
    }

    @Override
    public ListProperty<ConceptSpecification> modulePreferenceListForLanguageProperty() {
        return this.modulePreferenceListProperty;
    }

    @Override
    public LanguageCoordinateImmutable getLanguageCoordinate() {
        return getValue();
    }

    /**
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#descriptionTypePreferenceListProperty()
     */
    @Override
    public ListProperty<ConceptSpecification> descriptionTypePreferenceListProperty() {
        return this.descriptionTypePreferenceListProperty;
    }
    /**
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#dialectAssemblagePreferenceListProperty()
     */
    @Override
    public ListProperty<ConceptSpecification>  dialectAssemblagePreferenceListProperty() {
        return this.dialectAssemblagePreferenceListProperty;
    }


    /**
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#nextPriorityLanguageCoordinateProperty()
     */
    @Override
    public ObjectProperty<? extends ObservableLanguageCoordinate> nextPriorityLanguageCoordinateProperty() {
        return this.nextPriorityLanguageCoordinateProperty;
    }
    @Override
    public Optional<? extends LanguageCoordinate> getNextPriorityLanguageCoordinate() {
        return Optional.ofNullable(this.nextPriorityLanguageCoordinateProperty.getValue());
    }

    @Override
    public ObjectProperty<ConceptSpecification> languageConceptProperty() {
        return this.languageProperty;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ObservableLanguageCoordinateBase{" + this.getValue().toString() + '}';
    }

    @Override
    public LanguageCoordinateImmutable toLanguageCoordinateImmutable() {
        return getValue();
    }

}
