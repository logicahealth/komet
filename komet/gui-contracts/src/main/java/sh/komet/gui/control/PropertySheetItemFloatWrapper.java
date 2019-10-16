package sh.komet.gui.control;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

import java.util.Optional;

public class PropertySheetItemFloatWrapper implements PropertySheet.Item {

    private final String name;
    private final FloatProperty floatProperty;
    private ConceptSpecification propertySpecification = null;

    public PropertySheetItemFloatWrapper(String name, FloatProperty floatProperty) {
        if (floatProperty == null) {
            throw new NullPointerException("Float property cannot be null");
        }
        this.name = name;
        this.floatProperty = floatProperty;
    }

    public PropertySheetItemFloatWrapper(Manifold manifold,
                                         FloatProperty floatProperty) {
        this(manifold.getPreferredDescriptionText(new ConceptProxy(floatProperty.getName()),
                ConceptSpecification.getNameFromExternalString(floatProperty.getName())),
                floatProperty);
    }
    public ConceptSpecification getSpecification() {
        if (this.propertySpecification != null) {
            return this.propertySpecification;
        }
        return new ConceptProxy(this.floatProperty.getName());
    }

    public void setSpecification(ConceptSpecification propertySpecification) {
        this.propertySpecification = propertySpecification;
    }


    @Override
    public Class<?> getType() {
        return Float.class;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Enter the text for the version you wish to create";
    }

    @Override
    public Float getValue() {
        return floatProperty.get();
    }

    @Override
    public void setValue(Object value) {
        floatProperty.setValue((Float) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(floatProperty);
    }

}
