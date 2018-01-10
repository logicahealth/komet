package sh.komet.gui.control;

import javafx.collections.ObservableList;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import sh.isaac.api.Get;
import sh.komet.gui.drag.drop.IsaacClipboard;

public class CellConceptForDragDropControlWrapper extends ListCell<ConceptForControlWrapper> {

    public CellConceptForDragDropControlWrapper(){

        ListCell listCell = this;

        setOnDragDetected(event -> {

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ConceptForControlWrapper wrapper = this.getItem();
            IsaacClipboard content = new IsaacClipboard(Get.concept(wrapper));
            //content.putString(Integer.toString(this.getItem().getNid()));
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
                    !((ListCell) event.getGestureSource()).getListView().getId().equals(
                            ((ListCell) event.getGestureTarget()).getListView().getId())  ) {
                return;
            }

            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            //TODO: Change this to a shift down or Up rather than a index swap replace
            if (dragboard.hasString()) {
                ObservableList<ConceptForControlWrapper> items = getListView().getItems();

                int sourceSequence = Integer.parseInt(dragboard.getString());
                int targetSequence = this.getItem().getNid();
                int sourceIndex = findIndexOfConceptNid(sourceSequence);
                int targetIndex = findIndexOfConceptNid(targetSequence);
                ConceptForControlWrapper source = getConceptFromNid(sourceSequence);
                ConceptForControlWrapper target = getConceptFromNid(targetSequence);

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

    private int findIndexOfConceptNid(int conceptNid){
        int count = 0;
        for(ConceptForControlWrapper concept : getListView().getItems()){
            if(concept.getNid()== conceptNid) {
               return count;
            }
            count++;
        }
        return -1;
    }

    private ConceptForControlWrapper getConceptFromNid(int conceptNid){
        for(ConceptForControlWrapper concept : this.getListView().getItems()) {
           if(concept.getNid() == conceptNid) {
              return concept;
           }
        }
        return null;
    }
}
