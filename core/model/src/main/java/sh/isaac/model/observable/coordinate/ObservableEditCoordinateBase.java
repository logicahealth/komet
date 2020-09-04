package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

public abstract class ObservableEditCoordinateBase extends ObservableCoordinateImpl<EditCoordinateImmutable>
        implements ObservableEditCoordinate {
    /** The author property. */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> authorForChangesProperty;

    /** The default module property. */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> defaultModuleProperty;

    /** The promotion module property. */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> destinationModuleProperty;

    /** The path property. */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> promotionPathProperty;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptSpecification> authorForChangesListener = this::authorForChangesConceptChanged;
    private final ChangeListener<ConceptSpecification> defaultModuleListener = this::defaultModuleConceptChanged;
    private final ChangeListener<ConceptSpecification> destinationModuleListener = this::destinationModuleConceptChanged;
    private final ChangeListener<ConceptSpecification> promotionPathListener = this::promotionPathConceptChanged;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable edit coordinate impl.
     *
     * @param editCoordinate the edit coordinate
     */
    public ObservableEditCoordinateBase(EditCoordinate editCoordinate, String coordinateName) {
        super(editCoordinate.toEditCoordinateImmutable(), "Edit coordinate");
        this.authorForChangesProperty = makeAuthorForChangesProperty(editCoordinate);
        this.defaultModuleProperty = makeDefaultModuleProperty(editCoordinate);
        this.destinationModuleProperty = makeDestinationModuleProperty(editCoordinate);
        this.promotionPathProperty = makePromotionPathProperty(editCoordinate);
        addListeners();
    }

    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makePromotionPathProperty(EditCoordinate editCoordinate);

    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDefaultModuleProperty(EditCoordinate editCoordinate);

    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDestinationModuleProperty(EditCoordinate editCoordinate);

    protected abstract SimpleEqualityBasedObjectProperty<ConceptSpecification> makeAuthorForChangesProperty(EditCoordinate editCoordinate);

    protected void removeListeners() {
        this.authorForChangesProperty.removeListener(this.authorForChangesListener);
        this.defaultModuleProperty.removeListener(this.defaultModuleListener);
        this.destinationModuleProperty.removeListener(this.destinationModuleListener);
        this.promotionPathProperty.removeListener(this.promotionPathListener);
    }

    protected void addListeners() {
        this.authorForChangesProperty.addListener(this.authorForChangesListener);
        this.defaultModuleProperty.addListener(this.defaultModuleListener);
        this.destinationModuleProperty.addListener(this.destinationModuleListener);
        this.promotionPathProperty.addListener(this.promotionPathListener);
    }

    private void promotionPathConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                             ConceptSpecification old,
                                             ConceptSpecification newPathConcept) {
        this.setValue(EditCoordinateImmutable.make(getAuthorNidForChanges(),
                getDefaultModuleNid(),
                newPathConcept.getNid(),
                getDestinationModuleNid()));
    }

    private void authorForChangesConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                                ConceptSpecification oldAuthorConcept,
                                                ConceptSpecification newAuthorConcept) {
        this.setValue(EditCoordinateImmutable.make(newAuthorConcept.getNid(), getDefaultModuleNid(),
                getPromotionPath().getNid(),
                getDestinationModuleNid()));
    }

    private void defaultModuleConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                             ConceptSpecification old,
                                             ConceptSpecification newModuleConcept) {
        this.setValue(EditCoordinateImmutable.make(getAuthorNidForChanges(), newModuleConcept.getNid(),
                getPromotionPath().getNid(),
                getDestinationModuleNid()));
    }

    private void destinationModuleConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                             ConceptSpecification old,
                                             ConceptSpecification newModuleConcept) {
        this.setValue(EditCoordinateImmutable.make(getAuthorNidForChanges(), getDefaultModule().getNid(),
                getPromotionPath().getNid(),
                newModuleConcept.getNid()));
    }

    @Override
    public ObjectProperty<ConceptSpecification> authorForChangesProperty() {
        return this.authorForChangesProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> defaultModuleProperty() {
        return this.defaultModuleProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> promotionPathProperty() {
        return this.promotionPathProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> destinationModuleProperty() {
        return this.destinationModuleProperty;
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
