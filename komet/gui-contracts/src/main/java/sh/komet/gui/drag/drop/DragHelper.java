package sh.komet.gui.drag.drop;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.util.FxGet;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DragHelper {

    protected static final Logger LOG = LogManager.getLogger();

    private final Region region;
    private final Supplier<IdentifiedObject> objectToDragSupplier;
    private final Predicate<MouseEvent> acceptDrag;
    private boolean dragging = false;
    private Consumer<Boolean> dragNotifier = this::noOp;

    public DragHelper(Region region,
                      Supplier<IdentifiedObject> objectToDragSupplier,
                      Predicate<MouseEvent> acceptDrag, Consumer<Boolean> dragNotifier) {
        this(region,
                objectToDragSupplier,
                acceptDrag);
        this.dragNotifier = dragNotifier;
    }
    public DragHelper(Region region,
                             Supplier<IdentifiedObject> objectToDragSupplier,
                             Predicate<MouseEvent> acceptDrag) {
        this.region = region;
        this.objectToDragSupplier = objectToDragSupplier;
        this.acceptDrag = acceptDrag;
        region.setOnDragDetected(this::handleDragDetected);
        region.setOnDragDone(this::handleDragDone);
    }

    private void handleDragDetected(MouseEvent event) {
        LOG.debug("Drag detected: " + event);
        if (acceptDrag.test(event)) {
            this.dragging = true;
            this.dragNotifier.accept(this.dragging);
            Dragboard db = region.startDragAndDrop(TransferMode.COPY);
            IsaacClipboard content = new IsaacClipboard(objectToDragSupplier.get());

            DragImageMaker dragImageMaker = new DragImageMaker(region);
            db.setDragView(dragImageMaker.getDragImage());
            db.setContent(content);

            event.consume();
        }
    }

    private void handleDragDone(DragEvent event) {
        LOG.debug("Drag done: " + event);
        this.dragging = false;
        this.dragNotifier.accept(this.dragging);
        if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
            FxGet.dialogs().showInformationDialog("Unsupported transfer mode", "TransferMode.MOVE is not supported");
        }
        event.consume();
    }

    private void noOp(boolean value) {
        // noop.
    }
    public boolean isDragging() {
        return dragging;
    }
}
