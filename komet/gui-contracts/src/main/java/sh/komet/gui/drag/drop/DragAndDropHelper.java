package sh.komet.gui.drag.drop;

import javafx.geometry.Insets;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.jena.reasoner.rulesys.builtins.Drop;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.identity.IdentifiedObject;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DragAndDropHelper {
    final DragHelper dragHelper;
    final DropHelper dropHelper;

    public DragAndDropHelper(Region region,
                             Supplier<IdentifiedObject> objectToDragSupplier,
                             Consumer<IdentifiedObject> draggedObjectAcceptor,
                             Predicate<MouseEvent> acceptDrag,
                             Predicate<DragEvent> acceptDrop) {
        this.dragHelper = new DragHelper(region, objectToDragSupplier, acceptDrag);
        this.dropHelper = new DropHelper(region, draggedObjectAcceptor, acceptDrop, this.dragHelper::isDragging);
    }

}
