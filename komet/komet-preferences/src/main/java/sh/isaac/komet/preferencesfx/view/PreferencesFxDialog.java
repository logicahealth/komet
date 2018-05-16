package sh.isaac.komet.preferencesfx.view;

import sh.isaac.komet.preferencesfx.history.History;
import sh.isaac.komet.preferencesfx.history.view.HistoryDialog;
import sh.isaac.komet.preferencesfx.model.PreferencesFxModel;
import sh.isaac.komet.preferencesfx.util.Constants;
import sh.isaac.komet.preferencesfx.util.StorageHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the dialog which is used to show the PreferencesFX window.
 *
 * @author François Martin
 * @author Marco Sanfratello
 */
public class PreferencesFxDialog extends DialogPane {
  private static final Logger LOGGER =
      LogManager.getLogger(PreferencesFxDialog.class.getName());

  private PreferencesFxModel model;
  private PreferencesFxView preferencesFxView;

  private Dialog dialog = new Dialog();
  private StorageHandler storageHandler;
  private boolean persistWindowState;
  private boolean saveSettings;
  private ButtonType closeWindowBtnType = ButtonType.CLOSE;
  private ButtonType cancelBtnType = ButtonType.CANCEL;
  private final String title;

  /**
   * Initializes the {@link DialogPane} which shows the PreferencesFX window.
   *
   * @param title
   * @param model             the model of PreferencesFX
   * @param preferencesFxView the master view to be display in this {@link DialogPane}
   */
  public PreferencesFxDialog(String title, PreferencesFxModel model, PreferencesFxView preferencesFxView) {
    this.title = title;
    this.model = model;
    this.preferencesFxView = preferencesFxView;
    persistWindowState = model.isPersistWindowState();
    saveSettings = model.isSaveSettings();
    storageHandler = model.getStorageHandler();
    model.loadSettingValues();
    layoutForm();
    setupDialogClose();
    loadLastWindowState();
    setupButtons();
    dialog.show();
    if (model.getHistoryDebugState()) {
      setupDebugHistoryTable();
    }
  }

  private void layoutForm() {
    dialog.setTitle(this.title);
    dialog.setResizable(true);
    getButtonTypes().addAll(closeWindowBtnType, cancelBtnType);
    dialog.initModality(Modality.NONE);
    dialog.setDialogPane(this);
    setContent(preferencesFxView);
  }

  private void setupDialogClose() {
    dialog.setOnCloseRequest(e -> {
      if (persistWindowState) {
        saveWindowState();
      }
      if (saveSettings) {
        model.saveSettingValues();
      }
    });
  }

  private void saveWindowState() {
    storageHandler.saveWindowWidth(widthProperty().get());
    storageHandler.saveWindowHeight(heightProperty().get());
    storageHandler.saveWindowPosX(getScene().getWindow().getX());
    storageHandler.saveWindowPosY(getScene().getWindow().getY());
    storageHandler.saveDividerPosition(model.getDividerPosition());
    model.saveSelectedCategory();
  }

  /**
   * Loads last saved size and position of the window.
   */
  public void loadLastWindowState() {
    if (persistWindowState) {
      setPrefSize(storageHandler.loadWindowWidth(), storageHandler.loadWindowHeight());
      getScene().getWindow().setX(storageHandler.loadWindowPosX());
      getScene().getWindow().setY(storageHandler.loadWindowPosY());
      model.setDividerPosition(storageHandler.loadDividerPosition());
      model.setDisplayedCategory(model.loadSelectedCategory());
    } else {
      setPrefSize(Constants.DEFAULT_PREFERENCES_WIDTH, Constants.DEFAULT_PREFERENCES_HEIGHT);
      getScene().getWindow().centerOnScreen();
    }
  }

  private void setupDebugHistoryTable() {
    final KeyCombination keyCombination =
        new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    preferencesFxView.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if (keyCombination.match(event)) {
        LOGGER.trace("Opened History Debug View");
        new HistoryDialog(model.getHistory());
      }
    });
  }

  private void setupButtons() {
    LOGGER.trace("Setting Buttons up");
    final Button closeBtn = (Button) lookupButton(closeWindowBtnType);
    final Button cancelBtn = (Button) lookupButton(cancelBtnType);

    History history = model.getHistory();
    cancelBtn.setOnAction(event -> {
      LOGGER.trace("Cancel Button was pressed");
      history.clear(true);
      // save settings after undoing them
      if (saveSettings) {
        model.saveSettingValues();
      }
    });
    closeBtn.setOnAction(event -> {
      LOGGER.trace("Close Button was pressed");
      history.clear(false);
    });

    cancelBtn.visibleProperty().bind(model.buttonsVisibleProperty());
    closeBtn.visibleProperty().bind(model.buttonsVisibleProperty());
  }
}
