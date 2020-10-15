package sh.komet.gui.control.datetimepicker;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.skins.JFXGenericPickerSkin;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.time.LocalTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <h1>Material Design Time Picker Skin</h1>
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2017-03-01
 */
public class TimePickerSkin extends JFXGenericPickerSkin<LocalTime> {

	private static final Logger LOG = LogManager.getLogger();
	
    private TimePicker jfxTimePicker;
    // displayNode is the same as editorNode
    private TextField displayNode;
    private TimePickerContent content;
    private JFXDialog dialog;

    public TimePickerSkin(TimePicker timePicker) {
        super(timePicker);

        this.jfxTimePicker = timePicker;

        // add focus listener on editor node
        timePicker.focusedProperty().addListener(observable -> {
            if (getEditor() != null && !timePicker.isFocused()) {
                reflectSetTextFromTextFieldIntoComboBoxValue();
            }
        });

        updateArrow(timePicker);
        ((PickerTextField) getEditor()).setFocusColor(timePicker.getDefaultColor());

        registerChangeListener(timePicker.defaultColorProperty(), obs -> updateArrow(timePicker));
        registerChangeListener(timePicker.converterProperty(), obs -> reflectUpdateDisplayNode());
        registerChangeListener(timePicker.editorProperty(), obs -> reflectUpdateDisplayNode());
        registerChangeListener(timePicker.showingProperty(), obs -> {
            LOG.debug("Showing: " + obs);
            if (jfxTimePicker.isShowing()) {
                show();
            } else {
                hide();
            }
        });
        registerChangeListener(timePicker.valueProperty(), obs -> {
            reflectUpdateDisplayNode();
            jfxTimePicker.fireEvent(new ActionEvent());
        });
    }

    private void updateArrow(TimePicker picker) {
        ((Region) arrowButton.getChildren().get(0)).setBackground(new Background(
                new BackgroundFill(picker.getDefaultColor(), null, null)));
        ((PickerTextField) getEditor()).setFocusColor(jfxTimePicker.getDefaultColor());
    }

    @Override
    protected Node getPopupContent() {
        if (content == null) {
            content = new TimePickerContent((TimePicker) getSkinnable());
        }
        return content;
    }

    @Override
    public void show() {
        if (!jfxTimePicker.isOverLay()) {
            super.show();
        }
        if (content != null) {
            content.init();
            content.clearFocus();
        }
        if (dialog == null && jfxTimePicker.isOverLay()) {
            StackPane dialogParent = jfxTimePicker.getDialogParent();
            if (dialogParent == null) {
                dialogParent = (StackPane) jfxTimePicker.getScene().getRoot();
            }
            dialog = new JFXDialog(dialogParent, (Region) getPopupContent(),
                    DialogTransition.CENTER, true);
            arrowButton.setOnMouseClicked((click) -> {
                if (jfxTimePicker.isOverLay()) {
                    StackPane parent = jfxTimePicker.getDialogParent();
                    if (parent == null) {
                        parent = (StackPane) jfxTimePicker.getScene().getRoot();
                    }
                    dialog.show(parent);
                }
            });
        }
    }

    @Override
    protected TextField getEditor() {
        return ((TimePicker) getSkinnable()).getEditor();
    }

    @Override
    protected StringConverter<LocalTime> getConverter() {
        return ((TimePicker) getSkinnable()).getConverter();
    }

    @Override
    public Node getDisplayNode() {
        if (displayNode == null) {
            displayNode = reflectGetEditableInputNode();
            displayNode.getStyleClass().add("time-picker-display-node");
            reflectUpdateDisplayNode();
        }
        displayNode.setEditable(jfxTimePicker.isEditable());
        return displayNode;
    }
}

