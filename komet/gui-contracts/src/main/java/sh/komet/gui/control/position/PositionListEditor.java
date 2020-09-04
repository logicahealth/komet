package sh.komet.gui.control.position;

import com.jfoenix.controls.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.datetimepicker.PickADate;
import sh.komet.gui.control.datetimepicker.TimePicker;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import java.time.*;

import static sh.isaac.api.util.time.DateTimeUtil.EASY_TO_READ_DATE_TIME_FORMAT;

public class PositionListEditor implements PropertyEditor<ObservableList<StampPositionImmutable>> {

    private ObservableList<StampPositionImmutable> value;

    BorderPane editorPane = new BorderPane();
    StackPane rootStack = new StackPane(editorPane);
    AnchorPane anchorPane = new AnchorPane();
    PickADate datePicker = new PickADate();
    TimePicker timePicker = new TimePicker();
    ManifoldCoordinate manifoldCoordinate;
    ListView<PositionWrapper> positionListView = new ListView<>();
    {
        datePicker.setPrefWidth(152);
        datePicker.setMaxWidth(Long.MAX_VALUE);
        datePicker.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate date) {
                if (date != null) {
                    return "  " + DateTimeUtil.EASY_TO_READ_DATE_FORMAT.format(date);
                } else {
                    return "";
                }
            }

            @Override public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string);
                } else {
                    return null;
                }
            }
        });
        timePicker.setPrefWidth(152);
        timePicker.setMaxWidth(Long.MAX_VALUE);
        timePicker.set24HourView(true);
        timePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalTime time) {
                if (time != null) {
                    ZonedDateTime zdt = ZonedDateTime.of(LocalDate.now(), time, ZoneId.systemDefault());
                    return "  " + DateTimeUtil.EASY_TO_READ_TIME_FORMAT.format(zdt);
                } else {
                    return "";
                }
            }

            @Override
            public LocalTime fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalTime.parse(string);
                } else {
                    return null;
                }
            }
        });

        positionListView.setPrefHeight(250);
        positionListView.setMinHeight(250);
        positionListView.setMaxHeight(250);
        positionListView.setCellFactory(c-> new ListCell<>() {
            @Override
            protected void updateItem(PositionWrapper item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    this.setText(DateTimeUtil.format(item.position.getTime(), EASY_TO_READ_DATE_TIME_FORMAT) + "\non path: " +
                            manifoldCoordinate.getPreferredDescriptionText(item.position.getPathForPositionConcept()));
                } else {
                    this.setText("");
                }
            }

        });

        positionListView.getSelectionModel().selectionModeProperty().setValue(SelectionMode.SINGLE);
        positionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                this.doubleClick();
            }
        });
        editorPane.setTop(anchorPane);
        editorPane.setCenter(positionListView);
    }
    private static final int BUTTON_SIZE = 36;

    Button addButton = new Button("", Iconography.ADD.getIconographic());
    Button upButton = new Button("", Iconography.ARROW_UP.getIconographic());
    Button downButton = new Button("", Iconography.ARROW_DOWN.getIconographic());
    Button deleteButton = new Button("", Iconography.DELETE_TRASHCAN.getIconographic());
    {
        addButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        upButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        downButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        deleteButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);

        addButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        upButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        downButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        deleteButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

        addButton.setOnAction(this::addNewPosition);
        upButton.setContentDisplay(ContentDisplay.RIGHT);
        downButton.setContentDisplay(ContentDisplay.RIGHT);
        upButton.setOnAction(this::moveUpSelection);
        downButton.setOnAction(this::moveDownSelection);
    }
    private final ToolBar listToolbar = new ToolBar(addButton, upButton, downButton, deleteButton);
    {
        listToolbar.setOrientation(Orientation.VERTICAL);
        editorPane.setRight(listToolbar);
        deleteButton.setOnAction(this::deleteSelection);
    }
    JFXDialog dialog;
    ToggleGroup group = new ToggleGroup();
    RadioButton latestRadio = new RadioButton("latest");
    RadioButton fixedRadio = new RadioButton("fixed time");


    ComboBox<ConceptSnapshot> pathConceptComboBox = new ComboBox<>();
    Button changeButton = new Button("change");
    Button cancelButton = new Button("cancel");
    JFXDialogLayout dialogLayout = new JFXDialogLayout();
    {
        latestRadio.setToggleGroup(group);
        latestRadio.setPadding(new Insets(10,0,5,0));
        latestRadio.setOnAction(event -> {
            this.timePicker.setVisible(!latestRadio.isSelected());
            this.datePicker.setVisible(!latestRadio.isSelected());
        });
        fixedRadio.setToggleGroup(group);
        fixedRadio.setPadding(new Insets(0,0,5,0));
        fixedRadio.setOnAction(event -> {
            this.timePicker.setVisible(fixedRadio.isSelected());
            this.datePicker.setVisible(fixedRadio.isSelected());
            if (fixedRadio.isSelected()) {
                if (this.datePicker.getValue().getYear() > 9000) {
                    this.datePicker.setValue(LocalDate.now());
                    this.timePicker.setValue(LocalTime.now(ZoneId.systemDefault()));
                }
            }
        });
        fixedRadio.setSelected(true);

        pathConceptComboBox.setPrefWidth(152);
        pathConceptComboBox.setMaxWidth(Long.MAX_VALUE);
        pathConceptComboBox.setPadding(new Insets(0, 10, 0, 0));

        cancelButton.setOnAction(event -> dialog.close());
        changeButton.setOnAction(event -> {
           this.originChanged();
           this.dialog.close();
        });
        dialogLayout.setHeading(new Text("Edit origin"));
        GridPane gridPane = new GridPane();
        // new VBox(this.pathConceptComboBox, this.latestRadio, this.fixedRadio, this.timePicker, this.datePicker)
        //  Node child, int columnIndex, int rowIndex, int columnspan, int rowspan, HPos halignment, VPos valignment, Priority hgrow, Priority vgrow
        GridPane.setConstraints(this.pathConceptComboBox, 0, 0, 2, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        GridPane.setConstraints(this.latestRadio, 0, 1, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        GridPane.setConstraints(this.fixedRadio, 0, 2, 2, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
        GridPane.setConstraints(this.timePicker, 0, 3, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        GridPane.setConstraints(this.datePicker, 1, 3, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        gridPane.getChildren().addAll(this.pathConceptComboBox, this.latestRadio, this.fixedRadio, this.timePicker, this.datePicker);
        dialogLayout.setBody(gridPane);
        dialogLayout.setActions(cancelButton, changeButton);
    }

    public PositionListEditor(ManifoldCoordinate manifoldCoordinate, ObservableList<StampPositionImmutable> value) {
        this.manifoldCoordinate = manifoldCoordinate;
        this.value = value;
        setViewItems(value);
        this.pathConceptComboBox.setItems(FxGet.activeConceptMembers(TermAux.PATH_ASSEMBLAGE, this.manifoldCoordinate));
        this.pathConceptComboBox.getSelectionModel().select(Get.conceptSnapshot(TermAux.DEVELOPMENT_PATH, this.manifoldCoordinate));
        this.positionListView.getItems().addListener(new ListChangeListener<PositionWrapper>() {
            @Override
            public void onChanged(Change<? extends PositionWrapper> c) {
                    System.out.println("Changed: " + c);
                }
        });

    }



    private void doubleClick() {
        PositionWrapper wrapper = this.positionListView.getSelectionModel().getSelectedItem();
        if (wrapper.position.getTime() == Long.MAX_VALUE) {
            this.latestRadio.setSelected(true);
        } else {
            this.fixedRadio.setSelected(true);
        }
        this.datePicker.setValue(wrapper.position.getTimeAsInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        this.timePicker.setValue(wrapper.position.getTimeAsInstant().atZone(ZoneId.systemDefault()).toLocalTime());
        this.pathConceptComboBox.getSelectionModel().select(Get.conceptSnapshot(wrapper.position.getPathForPositionNid(), this.manifoldCoordinate));
        this.dialog = new JFXDialog(this.rootStack, dialogLayout, JFXDialog.DialogTransition.CENTER, true);

        this.dialog.show();
    }

    protected void setViewItems(ObservableList<StampPositionImmutable> value) {
        this.positionListView.getItems().clear();
        for (StampPositionImmutable item: value) {
            this.positionListView.getItems().add(new PositionWrapper(item));
        }
    }

    private void originChanged() {
        PositionWrapper positionWrapper = this.positionListView.getSelectionModel().getSelectedItem();
        if (latestRadio.isSelected()) {
            positionWrapper.position = StampPositionImmutable.make(Long.MAX_VALUE,
                    this.pathConceptComboBox.getSelectionModel().getSelectedItem().getNid());
        } else {
            LocalDateTime localDateTime = LocalDateTime.of(this.datePicker.getValue(), this.timePicker.getValue());
            positionWrapper.position = StampPositionImmutable.make(DateTimeUtil.toEpochMilliseconds(localDateTime),
                    this.pathConceptComboBox.getSelectionModel().getSelectedItem().getNid());
        }
        this.positionListView.refresh();
        updateValueList();
    }

    private void updateValueList() {
        this.value.clear();
        for (PositionWrapper wrapper: this.positionListView.getItems()) {
            this.value.add(wrapper.position);
        }
    }

    @Override
    public Node getEditor() {
        return rootStack;
    }

    @Override
    public ObservableList<StampPositionImmutable> getValue() {
        return value;
    }

    @Override
    public void setValue(ObservableList<StampPositionImmutable> value) {
        this.value = value;
        setViewItems(value);
    }

    private void addNewPosition(ActionEvent event) {
        ZonedDateTime nowInMinutes = ZonedDateTime.parse(ZonedDateTime.now().format(EASY_TO_READ_DATE_TIME_FORMAT),
                EASY_TO_READ_DATE_TIME_FORMAT);
        int index = this.positionListView.getItems().size();
        StampPositionImmutable newPosition = StampPositionImmutable.make(nowInMinutes.toInstant().toEpochMilli(),
                TermAux.DEVELOPMENT_PATH);
        this.value.add(newPosition);
        this.positionListView.getItems().add(new PositionWrapper(newPosition));
        this.datePicker.setValue(nowInMinutes.toLocalDate());
        this.timePicker.setValue(nowInMinutes.toLocalTime());
        this.positionListView.getSelectionModel().select(index);
    }

    private void deleteSelection(ActionEvent event) {
        int selectedIndex = this.positionListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > -1) {
            this.positionListView.getItems().remove(selectedIndex);
        }
        updateValueList();
    }

    private void moveUpSelection(ActionEvent event) {
        int selectedIndex = this.positionListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            PositionWrapper positionToMove = this.positionListView.getItems().remove(selectedIndex);
            this.positionListView.getItems().add(selectedIndex-1, positionToMove);
            this.positionListView.getSelectionModel().select(selectedIndex-1);
        }
        updateValueList();
    }

    private void moveDownSelection(ActionEvent event) {
        int selectedIndex = this.positionListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < this.positionListView.getItems().size() - 1) {
            PositionWrapper positionToMove = this.positionListView.getItems().remove(selectedIndex);
            this.positionListView.getItems().add(selectedIndex+1, positionToMove);
            this.positionListView.getSelectionModel().select(selectedIndex+1);
        }
        updateValueList();
    }

    private static class PositionWrapper {
        StampPositionImmutable position;

        public PositionWrapper(StampPositionImmutable position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "PositionWrapper{" + this.position.toString() + "}";
        }
    }
}