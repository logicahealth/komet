package sh.komet.gui.control.concept;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.collections.NidSet;
import sh.komet.gui.util.FxGet;

/**
 * 
 * @author kec
 * @deprecated Use PropertySheetItemConceptWrapper
 */
@Deprecated
public class PropertySheetItemConceptNidWrapper implements ConceptSpecification, PropertySheet.Item {

   private final ManifoldCoordinate manifoldCoordinate;
   private final String name;
   private final SimpleObjectProperty<ConceptForControlWrapper> observableWrapper;
   private final IntegerProperty conceptNidProperty;
   private final NidSet allowedValues = new NidSet();
   
   public PropertySheetItemConceptNidWrapper(ManifoldCoordinate manifoldCoordinate,
                                             IntegerProperty conceptNidProperty, int... allowedValues) {
      this(manifoldCoordinate, manifoldCoordinate.getPreferredDescriptionText(new ConceptProxy(conceptNidProperty.getName())), conceptNidProperty, allowedValues);
   }


   public PropertySheetItemConceptNidWrapper(ManifoldCoordinate manifoldCoordinate, String name,
                                             IntegerProperty conceptNidProperty, int... allowedValues) {
      this.manifoldCoordinate = manifoldCoordinate;
      this.name = name;
      this.conceptNidProperty = conceptNidProperty;
      if (allowedValues.length > 0 && conceptNidProperty.get() == 0) {
          this.conceptNidProperty.set(allowedValues[0]);
      }
      this.allowedValues.addAll(allowedValues);
      this.observableWrapper = new SimpleObjectProperty<>(new ConceptForControlWrapper(manifoldCoordinate, conceptNidProperty.get()));
      this.conceptNidProperty.addListener((observable, oldValue, newValue) -> {
          this.observableWrapper.setValue(new ConceptForControlWrapper(manifoldCoordinate, newValue.intValue()));
      });
   }
   
   

   @Override
   public String getFullyQualifiedName() {
      return this.manifoldCoordinate.getFullyQualifiedDescriptionText(conceptNidProperty.get());
   }

   @Override
   public Optional<String> getRegularName() {
      return Optional.of(manifoldCoordinate.getPreferredDescriptionText(conceptNidProperty.get()));
   }

   @Override
   public List<UUID> getUuidList() {
      return new ConceptProxy(conceptNidProperty.getName()).getUuidList();
   }

   @Override
   public Class<?> getType() {
      return ConceptForControlWrapper.class;
   }

   @Override
   public String getCategory() {
      return null;
   }

   public NidSet getAllowedValues() {
      return allowedValues;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getDescription() {
      return "Select the proper concept value for the version you wish to create. ";
   }

   @Override
   public ConceptForControlWrapper getValue() {
      return this.observableWrapper.get();
   }

   @Override
   public void setValue(Object value) {
      try {
         // Concept sequence property may throw a runtime exception if it cannot be changed
         this.conceptNidProperty.setValue(((ConceptForControlWrapper) value).getNid());         
      } catch (RuntimeException ex) {
         FxGet.statusMessageService().reportStatus(ex.getMessage());
         this.observableWrapper.setValue(new ConceptForControlWrapper(manifoldCoordinate, this.conceptNidProperty.get()));
      }
   }
   public void setDefaultValue(ConceptSpecification value) {
      try {
         // Concept sequence property may throw a runtime exception if it cannot be changed
         this.conceptNidProperty.setValue(value.getNid());         
      } catch (RuntimeException ex) {
         FxGet.statusMessageService().reportStatus(ex.getMessage());
         this.observableWrapper.setValue(new ConceptForControlWrapper(manifoldCoordinate, this.conceptNidProperty.get()));
      }
   }

   @Override
   public Optional<ObservableValue<? extends Object>> getObservableValue() {
      return Optional.of(this.conceptNidProperty);
   }
  
   public ConceptSpecification getSpecification() {
      return new ConceptProxy(this.conceptNidProperty.getName());
   }

   @Override
   public String toString() {
      return "Property sheet item for "
              + manifoldCoordinate.getPreferredDescriptionText(new ConceptProxy(conceptNidProperty.getName()));
   }
}
