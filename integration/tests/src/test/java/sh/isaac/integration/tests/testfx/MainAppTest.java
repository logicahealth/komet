package sh.isaac.integration.tests.testfx;

import sh.komet.fx.stage.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import org.testfx.api.FxToolkit;

public class MainAppTest extends ApplicationTest {

    @Override
    public void start (Stage stage) throws Exception {
        Parent mainNode = FXMLLoader.load(MainApp.class.getResource("/fxml/SelectDataSource.fxml"));
        stage.setScene(new Scene(mainNode));
        stage.initStyle(StageStyle.DECORATED);
        stage.show();
        stage.toFront();
        sleep(1000);
    }

    @Before
    public void setUp () throws Exception {
//        System.setProperty("testfx.headless", "true");
    }

    @After
    public void tearDown () throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @Test
    public void testEnglishInput() {
        clickOn("#dataSourceChoiceBox");
        type(KeyCode.DOWN);
        type(KeyCode.UP);
        sleep(1000);
        type(KeyCode.ENTER);
        clickOn("#fileListView");
        type(KeyCode.DOWN);
        type(KeyCode.UP);
        sleep(1000);
        type(KeyCode.ENTER);
        sleep(1000);
        //ClickOn function below is the next step, but throws a null pointer error
        //Null pointer error is most likely an issue with functions attempting to replace stage (i.e. MainApp.replacePrimaryStage)
        //It opens a new screen (StartUpAfterSelection), but the original window is still open
//        clickOn("#okButton");
    }
}
