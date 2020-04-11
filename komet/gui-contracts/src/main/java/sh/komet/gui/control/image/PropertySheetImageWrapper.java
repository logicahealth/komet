package sh.komet.gui.control.image;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

import java.util.Optional;

public class PropertySheetImageWrapper implements PropertySheet.Item {

    private final String name;
    private final ObjectProperty<byte[]> imageDataProperty;
    private ConceptSpecification propertySpecification = null;

    public PropertySheetImageWrapper(String name, ObjectProperty<byte[]> imageDataProperty) {
        if (imageDataProperty == null) {
            throw new NullPointerException("imageDataProperty cannot be null");
        }
        this.name = name;
        this.imageDataProperty = imageDataProperty;
    }

    public PropertySheetImageWrapper(Manifold manifold,
                                     ObjectProperty<byte[]> imageDataProperty) {
        this(manifold.getPreferredDescriptionText(new ConceptProxy(imageDataProperty.getName())),
                imageDataProperty);
    }
    public ConceptSpecification getSpecification() {
        if (this.propertySpecification != null) {
            return this.propertySpecification;
        }
        return new ConceptProxy(this.imageDataProperty.getName());
    }

    public void setSpecification(ConceptSpecification propertySpecification) {
        this.propertySpecification = propertySpecification;
    }


    @Override
    public Class<?> getType() {
        return byte[].class;
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
        return "Select the image data for the version you wish to create";
    }

    @Override
    public byte[] getValue() {
        return imageDataProperty.get();
    }

    @Override
    public void setValue(Object value) {
        imageDataProperty.setValue((byte[]) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(imageDataProperty);
    }

    public ObjectProperty<byte[]> imageDataProperty() {
        return imageDataProperty;
    }

}
