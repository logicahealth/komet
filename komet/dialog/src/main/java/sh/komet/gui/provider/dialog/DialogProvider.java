/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
package sh.komet.gui.provider.dialog;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ExceptionDialog;
import org.jvnet.hk2.annotations.Service;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import sh.komet.gui.contract.DialogService;

/**
 * CommonDialogs
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author kec
 */
@Service
@Singleton
public class DialogProvider implements DialogService {

   private final Logger LOG = LogManager.getLogger(this.getClass());

   private DialogProvider() throws IOException {
      // hidden - constructed by HK2
   }

   private Alert createAlertDialog(AlertType type, Modality modality) {
      return createAlertDialog(type, modality, null);
   }

   private Alert createAlertDialog(AlertType type, Modality modality, Window owner) {
      Alert dialog = new Alert(type, "");
      dialog.initModality(modality);
      dialog.initOwner(owner);
      dialog.setResizable(true);
      return dialog;
   }

   @Override
   public void showInformationDialog(String title, String message) {
      this.showInformationDialog(title, message, null);
   }

   @Override
   public void showInformationDialog(String title, Node content) {
      Runnable r = () -> {
         Alert informationDialog = createAlertDialog(AlertType.INFORMATION, Modality.NONE);
         informationDialog.setTitle("");
         informationDialog.getDialogPane().setHeaderText(title);
         informationDialog.getDialogPane().setContent(content);
         informationDialog.initStyle(StageStyle.UTILITY);
         informationDialog.setResizable(true);
         informationDialog.showAndWait();
      };
      show(r);
   }

   @Override
   public void showInformationDialog(String title, String message, Window parentWindow) {
      Runnable r = () -> {
         Alert informationDialog = createAlertDialog(AlertType.INFORMATION, Modality.NONE, parentWindow);
         informationDialog.setTitle("");
         informationDialog.getDialogPane().setHeaderText(title);
         informationDialog.getDialogPane().setContentText(message);
         informationDialog.initStyle(StageStyle.UTILITY);
         informationDialog.setResizable(true);
         informationDialog.showAndWait();
      };
      show(r);
   }

   private void show(Runnable r) {
      if (Platform.isFxApplicationThread()) {
         r.run();
      } else {
         Platform.runLater(r);
      }
   }

   @Override
   public void showErrorDialog(String message, Throwable throwable) {
      LOG.error(message, throwable);
      Runnable showDialog = () -> {
         ExceptionDialog dlg = new ExceptionDialog(throwable);
         dlg.setTitle(throwable.getClass().getName());
         dlg.setHeaderText(message);
         //dlg.getDialogPane().setHeaderText(throwable.getMessage());
         dlg.initStyle(StageStyle.UTILITY);
         dlg.setResizable(true);
         dlg.showAndWait();
      };
      show(showDialog);
   }

   @Override
   public void showErrorDialog(String title, String message, String details) {
      showErrorDialog(title, message, details, null);
   }

   @Override
   public void showErrorDialog(String title, String message, String details, Window parentWindow) {
      Runnable showDialog = () -> {
         Alert dlg = createAlertDialog(AlertType.ERROR, Modality.NONE, parentWindow);
         dlg.setTitle(title);
         dlg.getDialogPane().setHeaderText(message);
         dlg.getDialogPane().setContentText(details);
         dlg.setResizable(true);
         dlg.showAndWait();
      };

      show(showDialog);
   }

   @Override
   public Optional<ButtonType> showYesNoDialog(String title, String question) {
      return showYesNoDialog(title, question, null);
   }

   @Override
   public Optional<ButtonType> showYesNoDialog(String title, String question, Window parentWindow) {
      Callable<Optional<ButtonType>> showDialog = () -> {
         Alert dlg = createAlertDialog(AlertType.INFORMATION, Modality.NONE, parentWindow);
         dlg.setTitle("");
         dlg.getDialogPane().setHeaderText(title);
         dlg.getDialogPane().setContentText(question);
         dlg.getButtonTypes().clear();
         dlg.getButtonTypes().add(ButtonType.NO);
         dlg.getButtonTypes().add(ButtonType.YES);
         dlg.setResizable(true);
         return dlg.showAndWait();
      };
      final FutureTask<Optional<ButtonType>> showTask = new FutureTask<>(showDialog);

      show(showTask);
      try {
         return showTask.get();
      } catch (InterruptedException | ExecutionException ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public void showConceptDialog(UUID uuid) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void showConceptDialog(int conceptNID) {
      throw new UnsupportedOperationException();
   }

}
