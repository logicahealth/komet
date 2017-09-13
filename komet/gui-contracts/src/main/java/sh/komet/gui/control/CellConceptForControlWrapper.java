package sh.komet.gui.control;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import sh.komet.gui.control.ConceptForControlWrapper;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class CellConceptForControlWrapper extends ListCell<ConceptForControlWrapper> {

    public CellConceptForControlWrapper(){

        ListCell listCell = this;

        setOnDragDetected(event -> {

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(Integer.toString(this.getItem().getConceptSequence()));
            dragboard.setContent(content);
            dragboard.setDragView(this.snapshot(new SnapshotParameters(),null));
            event.consume();
        });

        setOnDragOver(event -> {

            if (event.getGestureSource() != listCell &&
                    event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();

        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != listCell &&
                    event.getDragboard().hasString()) {
                setOpacity(0.3);
            }
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != listCell &&
                    event.getDragboard().hasString()) {
                setOpacity(1);
            }
        });

        setOnDragDropped(event -> {

            if (getItem() == null ||
                    ((ListCell) event.getGestureSource()).getListView().getId()
                            != ((ListCell) event.getGestureTarget()).getListView().getId()  ) {
                return;
            }

            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            //TODO: Change this to a shift down or Up rather than a index swap replace
            if (dragboard.hasString()) {
                ObservableList<ConceptForControlWrapper> items = getListView().getItems();

                int sourceSequence = Integer.parseInt(dragboard.getString());
                int targetSequence = this.getItem().getConceptSequence();
                int sourceIndex = findIndexOfConceptSequence(sourceSequence);
                int targetIndex = findIndexOfConceptSequence(targetSequence);
                ConceptForControlWrapper source = getConceptFromSequence(sourceSequence);
                ConceptForControlWrapper target = getConceptFromSequence(targetSequence);

                items.set(targetIndex, source);
                items.set(sourceIndex, target);

                success = true;
            }
            event.setDropCompleted(success);
            event.consume();


        });

        setOnDragDone(DragEvent::consume);

    }

    @Override
    protected void updateItem(ConceptForControlWrapper item, boolean empty) {
        super.updateItem(item, empty);

        if(item == null){
            this.setText("");
        }else{
            this.setText(item.toString());
        }

    }

    private int findIndexOfConceptSequence(int conceptSequence){
        int count = 0;
        for(ConceptForControlWrapper concept : getListView().getItems()){
            if(concept.getConceptSequence() == conceptSequence)
                return count;
            count++;
        }
        return -1;
    }

    private ConceptForControlWrapper getConceptFromSequence(int conceptSequence){
        for(ConceptForControlWrapper concept : this.getListView().getItems())
            if(concept.getConceptSequence() == conceptSequence)
                return concept;
        return null;
    }
}
