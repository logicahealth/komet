/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.javascript;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kec
 */
public class JavascriptController {
    private static final Logger              LOG               = LogManager.getLogger();

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    
    @FXML
    private AnchorPane topPane;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button runButton;

    @FXML
    private TextArea scriptText;

    @FXML
    private TextArea resultText;

    @FXML
    void initialize() {
        assert topPane != null : "fx:id=\"topPane\" was not injected: check your FXML file 'scriptrunner.fxml'.";
        assert runButton != null : "fx:id=\"runButton\" was not injected: check your FXML file 'scriptrunner.fxml'.";
        assert scriptText != null : "fx:id=\"scriptText\" was not injected: check your FXML file 'scriptrunner.fxml'.";
        assert resultText != null : "fx:id=\"resultText\" was not injected: check your FXML file 'scriptrunner.fxml'.";
        scriptText.setText("print(Packages.sh.isaac.api.Get.conceptDescriptionText(-2147483643));");
    }

    public AnchorPane getTopPane() {
        return topPane;
    }
    
    @FXML
    void runJavascript(ActionEvent event) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            engine.getContext().setWriter(pw);
            engine.eval(scriptText.getText());
            resultText.setText(sw.getBuffer().toString());
        } catch (ScriptException ex) {
            resultText.setText(ex.getMessage());
            LOG.error("script error", ex);
        }
    }    
}
