package sh.isaac.komet.preferencesfx;

import com.dlsc.formsfx.model.util.TranslationService;
import java.util.prefs.Preferences;
import sh.isaac.komet.preferencesfx.history.History;
import sh.isaac.komet.preferencesfx.model.Category;
import sh.isaac.komet.preferencesfx.model.PreferencesFxModel;
import sh.isaac.komet.preferencesfx.util.SearchHandler;
import sh.isaac.komet.preferencesfx.util.StorageHandler;
import sh.isaac.komet.preferencesfx.view.BreadCrumbPresenter;
import sh.isaac.komet.preferencesfx.view.BreadCrumbView;
import sh.isaac.komet.preferencesfx.view.CategoryController;
import sh.isaac.komet.preferencesfx.view.CategoryPresenter;
import sh.isaac.komet.preferencesfx.view.CategoryView;
import sh.isaac.komet.preferencesfx.view.NavigationPresenter;
import sh.isaac.komet.preferencesfx.view.NavigationView;
import sh.isaac.komet.preferencesfx.view.PreferencesFxDialog;
import sh.isaac.komet.preferencesfx.view.PreferencesFxPresenter;
import sh.isaac.komet.preferencesfx.view.PreferencesFxView;
import sh.isaac.komet.preferencesfx.view.UndoRedoBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.util.FxGet;

/**
 * Represents the main PreferencesFX class.
 *
 * @author François Martin
 * @author Marco Sanfratello
 */
public class PreferencesFx {
  private static final Logger LOGGER =
      LogManager.getLogger(PreferencesFx.class.getName());

  private PreferencesFxModel preferencesFxModel;

  private NavigationView navigationView;
  private NavigationPresenter navigationPresenter;

  private UndoRedoBox undoRedoBox;

  private BreadCrumbView breadCrumbView;
  private BreadCrumbPresenter breadCrumbPresenter;

  private CategoryController categoryController;

  private PreferencesFxView preferencesFxView;
  private PreferencesFxPresenter preferencesFxPresenter;

  private PreferencesFx(Class<?> saveClass, Category... categories) {
      this(FxGet.userNode(saveClass), categories);
  }
  private PreferencesFx(IsaacPreferences preferences, Category... categories) {
    // asciidoctor Documentation - tag::testMock[]
    preferencesFxModel = new PreferencesFxModel(
        new StorageHandler(preferences), new SearchHandler(), new History(), categories
    );
    // asciidoctor Documentation - end::testMock[]

    // setting values are only loaded if they are present already
    preferencesFxModel.loadSettingValues();

    undoRedoBox = new UndoRedoBox(preferencesFxModel.getHistory());

    breadCrumbView = new BreadCrumbView(preferencesFxModel, undoRedoBox);
    breadCrumbPresenter = new BreadCrumbPresenter(preferencesFxModel, breadCrumbView);

    categoryController = new CategoryController();
    initializeCategoryViews();
    // display initial category
    categoryController.setView(preferencesFxModel.getDisplayedCategory());

    navigationView = new NavigationView(preferencesFxModel);
    navigationPresenter = new NavigationPresenter(preferencesFxModel, navigationView);

    preferencesFxView = new PreferencesFxView(
        preferencesFxModel, navigationView, breadCrumbView, categoryController
    );
    preferencesFxPresenter = new PreferencesFxPresenter(preferencesFxModel, preferencesFxView);
  }

  /**
   * Creates the Preferences window.
   *
   * @param saveClass  the class which the preferences are saved as
   *                   Must be unique to the application using the preferences
   * @param categories the items to be displayed in the TreeSearchView
   * @return the preferences window
   */
  public static PreferencesFx of(Class<?> saveClass, Category... categories) {
    return new PreferencesFx(saveClass, categories);
  }

  public static PreferencesFx of(IsaacPreferences preferences, Category... categories) {
    return new PreferencesFx(preferences, categories);
  }

  /**
   * Prepares the CategoryController by creating CategoryView / CategoryPresenter pairs from
   * all Categories and loading them into the CategoryController.
   */
  private void initializeCategoryViews() {
    preferencesFxModel.getFlatCategoriesLst().forEach(category -> {
      CategoryView categoryView = new CategoryView(preferencesFxModel, category);
      CategoryPresenter categoryPresenter = new CategoryPresenter(
          preferencesFxModel, category, categoryView, breadCrumbPresenter
      );
      categoryController.addView(category, categoryView, categoryPresenter);
    });
  }

  /**
   * Shows the PreferencesFX dialog.
     * @param title
     * @param preferences
   */
  public void show(String title) {
    new PreferencesFxDialog(title, preferencesFxModel, preferencesFxView);
  }

  /**
   * Defines if the PreferencesAPI should save the applications states.
   * This includes the persistence of the dialog window, as well as each settings values.
   *
   * @param enable if true, the storing of the window state of the dialog window
   *               and the settings values are enabled.
   * @return this object for fluent API
   */
  public PreferencesFx persistApplicationState(boolean enable) {
    persistWindowState(enable);
    saveSettings(enable);
    return this;
  }

  /**
   * Defines whether the state of the dialog window should be persisted or not.
   *
   * @param persist if true, the size, position and last selected item in the TreeSearchView are
   *                being saved. When the dialog is showed again, it will be restored to
   *                the last saved state. Defaults to false.
   * @return this object for fluent API
   */
  // asciidoctor Documentation - tag::fluentApiMethod[]
  public PreferencesFx persistWindowState(boolean persist) {
    preferencesFxModel.setPersistWindowState(persist);
    return this;
  }
  // asciidoctor Documentation - end::fluentApiMethod[]

  /**
   * Defines whether the adjusted settings of the application should be saved or not.
   *
   * @param save if true, the values of all settings of the application are saved.
   *             When the application is started again, the settings values will be restored to
   *             the last saved state. Defaults to false.
   * @return this object for fluent API
   */
  public PreferencesFx saveSettings(boolean save) {
    preferencesFxModel.setSaveSettings(save);
    // if settings shouldn't be saved, clear them if there are any present
    if (!save) {
      preferencesFxModel.getStorageHandler().clearPreferences();
    }
    return this;
  }

  /**
   * Defines whether the table to debug the undo / redo history should be shown in a dialog
   * when pressing a key combination or not.
   * <\br>
   * Pressing Ctrl + Shift + H (Windows) or CMD + Shift + H (Mac) opens a dialog with the
   * undo / redo history, shown in a table.
   *
   * @param debugState if true, pressing the key combination will open the dialog
   * @return this object for fluent API
   */
  public PreferencesFx debugHistoryMode(boolean debugState) {
    preferencesFxModel.setHistoryDebugState(debugState);
    return this;
  }

  public PreferencesFx buttonsVisibility(boolean isVisible) {
    preferencesFxModel.setButtonsVisible(isVisible);
    return this;
  }

  /**
   * Sets the translation service property of the preferences dialog.
   *
   * @param newValue The new value for the translation service property.
   * @return PreferencesFx to allow for chaining.
   */
  public PreferencesFx i18n(TranslationService newValue) {
    preferencesFxModel.setTranslationService(newValue);
    return this;
  }
}
