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
package sh.isaac.komet.gui;

import sh.isaac.komet.gui.dialog.DialogService;
import sh.isaac.komet.gui.drag.drop.DragRegistry;
import javafx.scene.Node;
import javafx.stage.Window;
import sh.isaac.api.Get;
import sh.isaac.api.identity.IdentifiedObject;

/**
 *
 * @author kec
 */
public class KOMET {
   
   public static DialogService dialogService() {
      return Get.service(DialogService.class);
   }
   
   public static void showErrorDialog(String message, Throwable throwable) {
      dialogService().showErrorDialog(message, throwable);
   }

   public static void showErrorDialog(final String title, final String message, final String details) {
       dialogService().showErrorDialog(title, message, details);
   }
   
   public static void showErrorDialog(final String title, final String message, final String details, final Window parentWindow) {
       dialogService().showErrorDialog(title, message, details, parentWindow);
   }
   
   public static void dragComplete() {
      Get.service(DragRegistry.class).conceptDragCompleted();
   }
   public static void dragStart() {
      Get.service(DragRegistry.class).conceptDragStarted();
   }
   
   public static void setupDragOnly(final Node n, IdentifiedObject idObject) {
      Get.service(DragRegistry.class).setupDragOnly(n, idObject);
   }
   
}
