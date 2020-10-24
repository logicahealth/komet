package sh.komet.gui;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import sh.komet.gui.util.FxGet;

import java.util.function.Consumer;

public class CatchThrowableEventHandler implements Consumer<ActionEvent> {
    final Consumer<ActionEvent> innerEventHandler;

    public CatchThrowableEventHandler(Consumer<ActionEvent> innerEventHandler) {
        this.innerEventHandler = innerEventHandler;
    }


    @Override
    public void accept(ActionEvent actionEvent) {
        try {
            this.innerEventHandler.accept(actionEvent);
        } catch (Exception e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }
}
