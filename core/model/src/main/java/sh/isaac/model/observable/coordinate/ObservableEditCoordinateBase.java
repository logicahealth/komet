package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

public abstract class ObservableEditCoordinateBase extends ObservableCoordinateImpl<EditCoordinateImmutable>
        implements ObservableEditCoordinate {
    /** The author property. */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> authorProperty;

    /** The module property. */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> moduleProperty;

    /** The path property. */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> pathProperty;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptSpecification> authorListener = this::authorConceptChanged;
    private final ChangeListener<ConceptSpecification> moduleListener = this::moduleConceptChanged;
    private final ChangeListener<ConceptSpecification> pathListener = this::pathConceptChanged;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable edit coordinate impl.
     *
     * @param editCoordinate the edit coordinate
     */
    public ObservableEditCoordinateBase(EditCoordinate editCoordinate, String coordinateName) {
        super(editCoordinate.toEditCoordinateImmutable(), "Edit coordinate");
        this.authorProperty = makeAuthorProperty(editCoordinate);
        this.moduleProperty = makeModuleProperty(editCoordinate);
        this.pathProperty = makePathProperty(editCoordinate);
        addListeners();
    }

    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makePathProperty(EditCoordinate editCoordinate);

    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makeModuleProperty(EditCoordinate editCoordinate);

    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makeAuthorProperty(EditCoordinate editCoordinate);

    protected void removeListeners() {
        this.moduleProperty.removeListener(this.moduleListener);
        this.authorProperty.removeListener(this.authorListener);
        this.pathProperty.removeListener(this.pathListener);
    }

    protected void addListeners() {
        this.moduleProperty.addListener(this.moduleListener);
        this.authorProperty.addListener(this.authorListener);
        this.pathProperty.addListener(this.pathListener);
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends EditCoordinateImmutable> observable, EditCoordinateImmutable oldValue, EditCoordinateImmutable newValue) {
        this.authorProperty.setValue(newValue.getAuthor());
        this.moduleProperty.setValue(newValue.getModule());
        this.pathProperty.setValue(newValue.getPath());
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                    ConceptSpecification old,
                                    ConceptSpecification newPathConcept) {
        this.setValue(EditCoordinateImmutable.make(getAuthorNid(), getModuleNid(), newPathConcept.getNid()));
    }

    private void authorConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                      ConceptSpecification oldAuthorConcept,
                                      ConceptSpecification newAuthorConcept) {
        this.setValue(EditCoordinateImmutable.make(newAuthorConcept.getNid(), getModuleNid(), getPathNid()));
    }

    private void moduleConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                      ConceptSpecification old,
                                      ConceptSpecification newModuleConcept) {
        this.setValue(EditCoordinateImmutable.make(getAuthorNid(), newModuleConcept.getNid(), getPathNid()));
    }

    @Override
    public ObjectProperty<ConceptSpecification> authorProperty() {
        return this.authorProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> moduleProperty() {
        return this.moduleProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> pathProperty() {
        return this.pathProperty;
    }

    @Override
    public EditCoordinate getEditCoordinate() {
        return this.getValue();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ObservableEditCoordinateImpl{" + this.getValue().toString() + '}';
    }

}
