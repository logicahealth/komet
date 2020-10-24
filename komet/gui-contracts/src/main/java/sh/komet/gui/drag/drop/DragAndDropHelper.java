package sh.komet.gui.drag.drop;

import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import sh.isaac.api.identity.IdentifiedObject;

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
