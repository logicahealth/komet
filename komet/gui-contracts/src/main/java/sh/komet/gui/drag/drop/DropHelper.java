package sh.komet.gui.drag.drop;

import javafx.geometry.Insets;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.identity.IdentifiedObject;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DropHelper {

    private final Region region;
    private Background originalBackground;
    private TransferMode[] transferMode = null;
    private final Consumer<IdentifiedObject> draggedObjectAcceptor;
    private final Predicate<DragEvent> acceptDrop;
    private final BooleanSupplier dragInProgress;

    public DropHelper(Region region,
                      Consumer<IdentifiedObject> draggedObjectAcceptor,
                      Predicate<DragEvent> acceptDrop,
                      BooleanSupplier dragInProgress) {
        this.region = region;
        this.draggedObjectAcceptor = draggedObjectAcceptor;
        this.acceptDrop = acceptDrop;
        this.dragInProgress = dragInProgress;
        region.setOnDragOver(this::handleDragOver);
        region.setOnDragEntered(this::handleDragEntered);
        region.setOnDragExited(this::handleDragExited);
        region.setOnDragDropped(this::handleDragDropped);
    }



    private void handleDragDropped(DragEvent event) {
        System.out.println("Dragging dropped: " + event);
        if (this.dragInProgress.getAsBoolean()) {
            return;
        }
        if (!this.acceptDrop.test(event)) {
            return;
        }
        Dragboard db = event.getDragboard();

        if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT)) {
            ConceptChronology conceptChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT);

            this.draggedObjectAcceptor.accept(conceptChronology);
        } else if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT_VERSION)) {
            ConceptVersion conceptVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT_VERSION);

            this.draggedObjectAcceptor.accept(conceptVersion.getChronology());
        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION)) {
            SemanticChronology semanticChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION);

            this.draggedObjectAcceptor.accept(Get.conceptService()
                    .getConceptChronology(semanticChronology.getReferencedComponentNid()));
        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION_VERSION)) {
            DescriptionVersion descriptionVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION_VERSION);

            this.draggedObjectAcceptor.accept(
                    Get.conceptService()
                            .getConceptChronology(descriptionVersion.getReferencedComponentNid()));
        }

        region.setBackground(originalBackground);
    }

    private void handleDragEntered(DragEvent event) {
        System.out.println("Dragging entered: " + event);
        if (this.dragInProgress.getAsBoolean()) {
            return;
        }
        if (!this.acceptDrop.test(event)) {
            return;
        }
        this.originalBackground = region.getBackground();

        Color backgroundColor;
        Set<DataFormat> contentTypes = event.getDragboard()
                .getContentTypes();

        if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.CONCEPT_TYPES)) {
            backgroundColor = Color.AQUA;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.DESCRIPTION_TYPES)) {
            backgroundColor = Color.OLIVEDRAB;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else {
            backgroundColor = Color.RED;
            this.transferMode = null;
        }

        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);

        region.setBackground(new Background(fill));
    }

    private void handleDragExited(DragEvent event) {
        System.out.println("Dragging exited: " + event);
        if (this.dragInProgress.getAsBoolean()) {
            return;
        }
        region.setBackground(originalBackground);
        this.transferMode = null;
    }

    private void handleDragOver(DragEvent event) {
        // System.out.println("Dragging over: " + event );
        if (this.dragInProgress.getAsBoolean()) {
            return;
        }
        if (!this.acceptDrop.test(event)) {
            return;
        }
        if (this.transferMode != null) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        }
    }
}
