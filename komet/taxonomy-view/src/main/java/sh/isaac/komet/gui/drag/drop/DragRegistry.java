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
package sh.isaac.komet.gui.drag.drop;

import sh.isaac.komet.gui.KOMET;
import sh.isaac.komet.gui.fx.FxUtils;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.effect.Effect;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.identity.IdentifiedObject;

/**
 * {@link DragRegistry}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author kec
 */
@Service
@Singleton
public class DragRegistry {
   private static final Logger LOG = LogManager.getLogger();

   private final AtomicLong dragStartedAt = new AtomicLong();
   ScheduledFuture<?> timedDragCancel;
   private final Set<Node> codeDropTargets = Collections.newSetFromMap(new WeakHashMap<>());
   private final WeakHashMap<Node, Effect> existingEffect = new WeakHashMap<>();

   private DragRegistry() {
      // created by HK2
      LOG.debug("Drag Registry init");
   }

   private void addConceptDropTargetInternal(Node node) {
      codeDropTargets.add(node);
   }

   public void removeDragCapability(Node n) {
      n.setOnDragDetected(null);
      n.setOnDragDone(null);
      n.setOnDragOver(null);
      n.setOnDragEntered(null);
      n.setOnDragExited(null);
      n.setOnDragDropped(null);
      codeDropTargets.remove(n);
      n.setEffect(existingEffect.remove(n));
   }

   //TODO Is there a way to set this up in constructors without getting the leaking in constructor message? 
   public void setupDragOnly(final Node n, IdentifiedObject idObject) {
      LOG.trace("Configure drag support for node {}", n);
      n.setOnDragDetected(new DragDetectedEventHandler(n, idObject));
      n.setOnDragDone(new DragDoneEventHandler());
   }

   public void setupDragAndDrop(final ComboBox<?> n, IdentifiedObject idObject, boolean allowDrop) {
      LOG.trace("Configure drag and drop for node {} - allow Drop {}", n, allowDrop);
      if (allowDrop) {
         addConceptDropTargetInternal(((Node) n));
         setDropShadows(n);
         n.setOnDragDropped((DragEvent event) -> {
            /* data dropped */
            Dragboard db = event.getDragboard();
            boolean success = false;
            try {
               if (db.hasString()) {
                  n.getEditor().setText(db.getString());
                  //not sure why I have to do this...
                  n.getEditor().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                  n.getEditor().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                  success = true;
                  // It will have updated its effect upon the set - we don't want to restore an old one.
                  existingEffect.remove(n);
               }
            } catch (Exception ex) {
               LOG.error("Error dropping snomed concept", ex);
               KOMET.showErrorDialog("Unexpected Error", "There was an unexpected error dropping the concept", ex.toString());
            }
            /*
            * let the source know whether the string was successfully transferred and used
            */
            event.setDropCompleted(success);
            event.consume();
         });
      }

      n.setOnDragDetected(new DragDetectedEventHandler(n, idObject));
      n.setOnDragDone(new DragDoneEventHandler());
   }

   public void setupDragAndDrop(final TextField n, IdentifiedObject idObject, boolean allowDrop) {
      LOG.trace("Configure drag and drop for node {} - allow Drop {}", n, allowDrop);

      if (allowDrop) {
         addConceptDropTargetInternal((Node) n);
         setDropShadows(n);
         n.setOnDragDropped((DragEvent event) -> {
            /* data dropped */
            Dragboard db = event.getDragboard();
            boolean success = false;
            try {
               if (db.hasString()) {
                  n.setText(db.getString());
                  //not sure why I have to do this...
                  n.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                  n.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                  success = true;
               }
            } catch (Exception ex) {
               LOG.error("Error dropping snomed concept", ex);
               KOMET.showErrorDialog("Unexpected Error", "There was an unexpected error dropping the concept", ex.toString());
            }
            /*
            * let the source know whether the string was successfully transferred and used
            */
            event.setDropCompleted(success);
            event.consume();
         });
      }
      n.setOnDragDetected(new DragDetectedEventHandler(n, idObject));
      n.setOnDragDone(new DragDoneEventHandler());
   }

   public void setupDragAndDrop(final Labeled n, IdentifiedObject idObject, boolean allowDrop) {
      setupDragAndDrop(n, idObject, allowDrop, null);
   }

   public void setupDragAndDrop(final Labeled n, IdentifiedObject idObject, boolean allowDrop, Function<String, String> passedDroppedStringHandler) {
      LOG.trace("Configure drag and drop for node {} - allow Drop {}", n, allowDrop);

      Function<String, String> droppedStringHandler = passedDroppedStringHandler != null ? passedDroppedStringHandler : (str) -> str;
      if (allowDrop) {
         addConceptDropTargetInternal((Node) n);
         setDropShadows(n);
         n.setOnDragDropped((DragEvent event) -> {
            /* data dropped */
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
               final String str = db.getString();
               
               Task<String> task = new Task<String>() {
                  @Override
                  protected String call() throws Exception {
                     return droppedStringHandler.apply(str);
                  }
                  
                  @Override
                  public void succeeded() {
                     Platform.runLater(() -> {
                        n.setText(getValue());
                        
                        n.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                        n.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                        /*
                        * let the source know whether the string was successfully transferred and used
                        */
                        event.setDropCompleted(true);
                        event.consume();
                     });
                  }
                  
                  @Override
                  public void failed() {
                     Platform.runLater(() -> {
                        n.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                        n.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                        
                        LOG.error("Error dropping snomed concept", getException());
                        KOMET.showErrorDialog("Unexpected Error", "There was an unexpected error dropping concept " + str, getException().toString());
                        
                        /*
                        * let the source know whether the string was successfully transferred and used
                        */
                        event.setDropCompleted(false);
                        event.consume();
                     });
                  }
                  
                  @Override
                  public void cancelled() {
                     Platform.runLater(() -> {
                        n.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                        n.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.CHAR_UNDEFINED, null, KeyCode.ENTER, false, false, false, false));
                        /*
                        * let the source know whether the string was successfully transferred and used
                        */
                        event.setDropCompleted(false);
                        event.consume();
                     });
                  }
               };
               Get.executor().execute(task);
            }
         });
      }
      n.setOnDragDetected(new DragDetectedEventHandler(n, idObject));
      n.setOnDragDone(new DragDoneEventHandler());
   }

   private void setDropShadows(final Node n) {
      n.setOnDragOver((DragEvent event) -> {
         LOG.debug("Drag Over node {}" + n);
         /*
         * data is dragged over the target accept it only if it is not dragged from the same node and if it has a string data
         */
         if (event.getGestureSource() != n && event.getDragboard().hasString()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
         }
         event.consume();
      });

      n.setOnDragEntered((DragEvent event) -> {
         LOG.debug("Drag Entered node {}" + n);
         /* show to the user that it is an actual gesture target */
         n.setEffect(FxUtils.GREEN_DROP_SHADOW);
         event.consume();
      });

      n.setOnDragExited((DragEvent event) -> {
         LOG.debug("Drag Exited node {}" + n);
         /* mouse moved away, remove the graphical cues */
         n.setEffect(FxUtils.LIGHT_GREEN_DROP_SHADOW);
         event.consume();
      });
   }

   public synchronized void conceptDragStarted() {
      LOG.debug("Drag Started");
      // There is a bug in javafx with comboboxes - it seems to fire dragStarted events twice.
      // http://javafx-jira.kenai.com/browse/RT-28778
      if ((System.currentTimeMillis() - dragStartedAt.get()) < 2000) {
         LOG.debug("Ignoring duplicate drag started event");
         return;
      }
      if (dragStartedAt.get() > 0) {
         LOG.warn("Unclosed drag event is still active while another was started!  Cleaning up...");
         conceptDragCompleted();
      }
      dragStartedAt.set(System.currentTimeMillis());
      for (Node n : codeDropTargets) {
         Effect existing = n.getEffect();
         if (existing != null) {
            existingEffect.put(n, existing);
         }
         n.setEffect(FxUtils.LIGHT_GREEN_DROP_SHADOW);
      }
      timedDragCancel = Get.scheduledExecutor().schedule(()
              -> {
         if (dragStartedAt.get() > 0) {
            LOG.warn("Unclosed drag event is still active 10 seconds after starting!  Cleaning up...");
            Platform.runLater(() -> {
               conceptDragCompleted();
            });
         }
      }, 10, TimeUnit.SECONDS);
   }

   public synchronized void conceptDragCompleted() {
      LOG.debug("Drag Completed");
      dragStartedAt.set(0);
      codeDropTargets.forEach((n) -> {
         n.setEffect(existingEffect.remove(n));
      });
      if (timedDragCancel != null) {
         timedDragCancel.cancel(false);
         timedDragCancel = null;
      }
   }
}
