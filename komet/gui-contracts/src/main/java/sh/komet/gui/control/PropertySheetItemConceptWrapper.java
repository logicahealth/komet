package sh.komet.gui.control;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.komet.gui.util.FxGet;

public class PropertySheetItemConceptWrapper implements ConceptSpecification, PropertySheet.Item {

   private final Manifold manifoldForDisplay;
   private final String name;
   private final SimpleObjectProperty<ConceptForControlWrapper> observableWrapper;
   private final IntegerProperty conceptSequenceProperty;
   private final ConceptSequenceSet allowedValues = new ConceptSequenceSet();

   public PropertySheetItemConceptWrapper(Manifold manifoldForDisplay, String name,
           IntegerProperty conceptSequenceProperty) {
      this.manifoldForDisplay = manifoldForDisplay;
      this.name = name;
      this.observableWrapper = new SimpleObjectProperty<>(new ConceptForControlWrapper(manifoldForDisplay, conceptSequenceProperty.get()));
      this.conceptSequenceProperty = conceptSequenceProperty;
   }

   @Override
   public String getFullySpecifiedConceptDescriptionText() {
      return this.manifoldForDisplay.getFullySpecifiedDescriptionText(conceptSequenceProperty.get());
   }

   @Override
   public Optional<String> getPreferedConceptDescriptionText() {
      return Optional.of(manifoldForDisplay.getPreferredDescriptionText(conceptSequenceProperty.get()));
   }

   @Override
   public List<UUID> getUuidList() {
      return new ConceptProxy(conceptSequenceProperty.getName()).getUuidList();
   }

   @Override
   public Class<?> getType() {
      return ConceptForControlWrapper.class;
   }

   @Override
   public String getCategory() {
      return null;
   }

   public ConceptSequenceSet getAllowedValues() {
      return allowedValues;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getDescription() {
      return "Tooltip for the property sheet item we are editing. ";
   }

   @Override
   public ConceptForControlWrapper getValue() {
      return this.observableWrapper.get();
   }

   @Override
   public void setValue(Object value) {
      try {
         // Concept sequence property may throw a runtime exception if it cannot be changed
         this.conceptSequenceProperty.setValue(((ConceptForControlWrapper) value).getConceptSequence());
         // only change the observableWrapper if no exception is thrown. 
         this.observableWrapper.setValue((ConceptForControlWrapper) value);
      } catch (RuntimeException ex) {
         FxGet.statusMessageService().reportStatus(ex.getMessage());
         this.observableWrapper.setValue(new ConceptForControlWrapper(manifoldForDisplay, this.conceptSequenceProperty.get()));
      }
   }

   @Override
   public Optional<ObservableValue<? extends Object>> getObservableValue() {
      return Optional.of(this.conceptSequenceProperty);
   }
  
   public ConceptSpecification getPropertySpecification() {
      return new ConceptProxy(this.conceptSequenceProperty.getName());
   }

   @Override
   public String toString() {
      return "Property sheet item for "
              + manifoldForDisplay.getPreferredDescriptionText(new ConceptProxy(conceptSequenceProperty.getName()));
   }
}
