/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.ddo.progress;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javafx.concurrent.Worker;

/**
 *
 * @author kec
 */
public class AggregateProgressItem implements Worker<Void> {

   /** Field description */
   private final ObjectProperty<Throwable> exceptionProperty = new SimpleObjectProperty<>(this, "exception");

   /** Field description */
   private final ObjectProperty<Void> valueProperty = new SimpleObjectProperty<>(this, "value", null);

   /** Field description */
   private final DoubleProperty progressProperty = new SimpleDoubleProperty(this, "progress", -1);

   /** Field description */
   private final DoubleProperty totalWorkProperty = new SimpleDoubleProperty(this, "totalWork", -1);

   /** Field description */
   private final DoubleProperty workDoneProperty = new SimpleDoubleProperty(this, "workDone", -1);

   /** Field description */
   private final BooleanProperty runningProperty = new SimpleBooleanProperty(this, "running", false);

   /** Field description */
   private final ObjectProperty<State> stateProperty = new SimpleObjectProperty<>(this, "state", State.READY);

   /** A list of subordinate progress items if this item is part of an aggregate task. */
   private Worker<?>[] subordinates;

   /** Field description */
   private ReadOnlyDoubleProperty[] progressProperties;

   /** Field description */
   private ReadOnlyDoubleProperty[] totalWorkProperties;

   /** Field description */
   private ReadOnlyDoubleProperty[] workDoneProperties;

   /** Field description */
   private ReadOnlyObjectProperty<Worker.State>[] stateProperties;

   /** Field description */
   private ReadOnlyObjectProperty<Throwable>[] exceptionProperties;

   /** Field description */
   private ReadOnlyBooleanProperty[] runningProperties;

   /** Field description */
   private DoubleBinding progressBinding;

   /** Field description */
   private DoubleBinding totalWorkBinding;

   /** Field description */
   private DoubleBinding workDoneBinding;

   /** Field description */
   private AggregateStateBinding stateBinding;

   /** Field description */
   private BooleanOrBinding runningBinding;

   /** Field description */
   private ThrowableBinding exceptionBinding;

   /** Field description */
   private final StringProperty messageProperty;

   /** Field description */
   private final StringProperty titleProperty;

   /**
    * Constructs ...
    * TODO add ability to dynamically add subordinates...
    *
    * @param title
    * @param message
    * @param subordinates
    */
   public AggregateProgressItem(String title, String message, Worker<?>... subordinates) {
      this.subordinates        = subordinates;
      this.titleProperty       = new SimpleStringProperty(this, "title", title);
      this.messageProperty     = new SimpleStringProperty(this, "message", message);
      this.progressProperties  = new ReadOnlyDoubleProperty[subordinates.length];
      this.totalWorkProperties = new ReadOnlyDoubleProperty[subordinates.length];
      this.workDoneProperties  = new ReadOnlyDoubleProperty[subordinates.length];
      this.stateProperties     = new ReadOnlyObjectProperty[subordinates.length];
      this.runningProperties   = new ReadOnlyBooleanProperty[subordinates.length];
      this.exceptionProperties   = new ReadOnlyObjectProperty[subordinates.length];

      for (int i = 0; i < subordinates.length; i++) {
         this.progressProperties[i]  = subordinates[i].progressProperty();
         this.totalWorkProperties[i] = subordinates[i].totalWorkProperty();
         this.workDoneProperties[i]  = subordinates[i].workDoneProperty();
         this.stateProperties[i]     = subordinates[i].stateProperty();
         this.runningProperties[i]   = subordinates[i].runningProperty();
         this.exceptionProperties[i] = subordinates[i].exceptionProperty();
      }

      this.progressBinding  = new DoubleAverageBinding(this.progressProperties);
      this.totalWorkBinding = new DoubleSumBinding(this.totalWorkProperties);
      this.workDoneBinding  = new DoubleSumBinding(this.workDoneProperties);
      this.stateBinding     = new AggregateStateBinding(this.stateProperties);
      this.runningBinding   = new BooleanOrBinding(this.runningProperties);
      this.exceptionBinding = new ThrowableBinding(this.exceptionProperties);
      this.progressProperty.bind(this.progressBinding);
      this.totalWorkProperty.bind(this.totalWorkBinding);
      this.workDoneProperty.bind(this.workDoneBinding);
      this.stateProperty.bind(this.stateBinding);
      this.runningProperty.bind(this.runningBinding);
      this.exceptionProperty.bind(this.exceptionBinding);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final boolean cancel() {
      boolean canceled = false;
System.out.println("AggregateProgressItem canceling");
      for (Worker<?> item : subordinates) {
         item.cancel();
      }

      return canceled;
   }

   /**
    * Method description
    *
    */
   private void checkThread() {
      if (!Platform.isFxApplicationThread()) {
         throw new IllegalStateException(
             "AggregateProgressItem must only be used from the FX Application Thread");
      }
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyObjectProperty<Throwable> exceptionProperty() {
      checkThread();

      return exceptionProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyStringProperty messageProperty() {
      checkThread();

      return messageProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyDoubleProperty progressProperty() {
      checkThread();

      return progressProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyBooleanProperty runningProperty() {
      checkThread();

      return runningProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyObjectProperty<State> stateProperty() {
      checkThread();

      return stateProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyStringProperty titleProperty() {
      checkThread();

      return titleProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyDoubleProperty totalWorkProperty() {
      checkThread();

      return totalWorkProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyObjectProperty<Void> valueProperty() {
      checkThread();

      return valueProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final ReadOnlyDoubleProperty workDoneProperty() {
      checkThread();

      return workDoneProperty;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final Throwable getException() {
      checkThread();

      return exceptionProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final String getMessage() {
      return messageProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final double getProgress() {
      checkThread();

      return progressProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final State getState() {
      checkThread();

      return stateProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public Worker<?>[] getSubordinates() {
      return subordinates;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final String getTitle() {
      return titleProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final double getTotalWork() {
      checkThread();

      return totalWorkProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final Void getValue() {
      checkThread();

      return null;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final double getWorkDone() {
      checkThread();

      return workDoneProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public final boolean isRunning() {
      checkThread();

      return runningProperty.get();
   }
}
