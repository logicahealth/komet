package sh.komet.gui.control.datetimepicker;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.skins.JFXGenericPickerSkin;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * <h1>Material Design Date Picker Skin</h1>
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2016-03-09
 */
public class DatePickerSkin extends JFXGenericPickerSkin<LocalDate> {

    /**
     * TODO:
     * 1. Handle different Chronology
     */
    private PickADate jfxDatePicker;

    // displayNode is the editorNode
    private TextField displayNode;
    private DatePickerContent content;
    private JFXDialog dialog;

    public DatePickerSkin(final PickADate datePicker) {
        super(datePicker);
        this.jfxDatePicker = datePicker;

        datePicker.focusedProperty().addListener(observable -> {
            if (getEditor() != null && !datePicker.isFocused()) {
                reflectSetTextFromTextFieldIntoComboBoxValue();
            }
        });

        // create calender or clock button
        updateArrow(datePicker);
        ((PickerTextField) getEditor()).setFocusColor(jfxDatePicker.getDefaultColor());

        registerChangeListener(datePicker.defaultColorProperty(), obs -> updateArrow(datePicker));
        registerChangeListener(datePicker.converterProperty(), obs -> reflectUpdateDisplayNode());
        registerChangeListener(datePicker.editableProperty(), obs -> reflectGetEditableInputNode());

        registerChangeListener(datePicker.dayCellFactoryProperty(), obs -> {
            reflectUpdateDisplayNode();
            content = null;
            popup = null;
        });

        registerChangeListener(datePicker.valueProperty(), obs -> {
            reflectUpdateDisplayNode();
            if (content != null) {
                LocalDate date = jfxDatePicker.getValue();
                content.displayedYearMonthProperty().set((date != null) ?
                        YearMonth.from(date) : YearMonth.now());
                content.updateValues();
            }
            jfxDatePicker.fireEvent(new ActionEvent());
        });
        registerChangeListener(datePicker.showWeekNumbersProperty(), obs -> {
            if (content != null) {
                // update the content grid to show week numbers
                content.updateContentGrid();
                content.updateWeekNumberDateCells();
            }
        });
        registerChangeListener(datePicker.showingProperty(), obs -> {
            if (jfxDatePicker.isShowing()) {
                if (content != null) {
                    LocalDate date = jfxDatePicker.getValue();
                    // set the current date / now when showing the date picker content
                    content.displayedYearMonthProperty().set((date != null) ?
                            YearMonth.from(date) : YearMonth.now());
                    content.updateValues();
                }
                show();
            } else {
                hide();
            }
        });
    }

    private void updateArrow(PickADate datePicker) {
        ((Region) arrowButton.getChildren().get(0)).setBackground(new Background(
                new BackgroundFill(datePicker.getDefaultColor(), null, null)));
        ((PickerTextField) getEditor()).setFocusColor(jfxDatePicker.getDefaultColor());
    }



    @Override
    protected Node getPopupContent() {
        if (content == null) {
            // different chronologies are not supported yet
            // will be called in constructor thus must use getSkinnable instead of jfxDatePicker
            content = new DatePickerContent(((PickADate) getSkinnable()));
        }
        return content;
    }

    @Override
    protected TextField getEditor() {
        return ((DatePicker) getSkinnable()).getEditor();
    }

    @Override
    protected StringConverter<LocalDate> getConverter() {
        return ((DatePicker) getSkinnable()).getConverter();
    }

    @Override
    public Node getDisplayNode() {
        if (displayNode == null) {
            displayNode = reflectGetEditableInputNode();
            displayNode.getStyleClass().add("date-picker-display-node");
            reflectUpdateDisplayNode();
        }
        displayNode.setEditable(jfxDatePicker.isEditable());
        return displayNode;
    }

    @Override
    public void show() {
        if (!jfxDatePicker.isOverLay()) {
            super.show();
        }
        if (content != null) {
            content.init();
            content.clearFocus();
        }
        if (dialog == null && jfxDatePicker.isOverLay()) {
            StackPane dialogParent = jfxDatePicker.getDialogParent();
            if (dialogParent == null) {
                dialogParent = (StackPane) jfxDatePicker.getScene().getRoot();
            }
            dialog = new JFXDialog(dialogParent, (Region) getPopupContent(), DialogTransition.CENTER, true);
            arrowButton.setOnMouseClicked((click) -> {
                if (jfxDatePicker.isOverLay()) {
                    StackPane parent = jfxDatePicker.getDialogParent();
                    if (parent == null) {
                        parent = (StackPane) jfxDatePicker.getScene().getRoot();
                    }
                    dialog.show(parent);
                }
            });
        }
    }
}