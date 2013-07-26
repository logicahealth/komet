/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.ddo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;

/**
 *
 * @author kec
 */
public class TimeReference implements Externalizable {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private ThreadLocal<SimpleDateFormat>                       formatter;
   private SimpleObjectProperty<ThreadLocal<SimpleDateFormat>> formatterProperty;
   private long                                                time;
   private SimpleLongProperty                                  timeProperty;
   private StringBinding                                       timeTextBinding;

   //~--- constructors --------------------------------------------------------

   public TimeReference() {}

   public TimeReference(long time) {
      this.time = time;
   }

   //~--- methods -------------------------------------------------------------

   public SimpleObjectProperty<ThreadLocal<SimpleDateFormat>> formatterProperty() {
      if (formatterProperty == null) {
         formatterProperty = new SimpleObjectProperty<>(this, "formatterProperty", getFormatter());
         timeTextBinding   = new StringBinding() {
            {
               super.bind(timeProperty(), formatterProperty);
            }
            @Override
            protected String computeValue() {
               return TimeHelper.formatDate(timeProperty.get(), formatterProperty.get());
            }
         };
      }

      return formatterProperty;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      time = in.readLong();

      if (in.readBoolean()) {
         String formatPattern = in.readUTF();

         formatter = TimeHelper.getFormatter(formatPattern);
      }
   }

   public SimpleLongProperty timeProperty() {
      if (timeProperty == null) {
         timeProperty = new SimpleLongProperty(this, "timeInMs", time);
      }

      return timeProperty;
   }

   @Override
   public String toString() {
      return getTimeText() + " (" + getTime() + ')';
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeLong(getTime());

      if ((formatterProperty != null) || (formatter != null)) {
         out.writeBoolean(true);
         out.writeUTF(getFormatter().get().toPattern());
      } else {
         out.writeBoolean(false);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private ThreadLocal<SimpleDateFormat> getFormatter() {
      if (formatterProperty == null) {
         if (formatter == null) {
            return TimeHelper.localShortFileFormat;
         }

         return formatter;
      }

      return formatterProperty.get();
   }

   public long getTime() {
      return (timeProperty == null)
             ? time
             : timeProperty.get();
   }

   public String getTimeText() {
      if (timeTextBinding != null) {
         return timeTextBinding.get();
      }

      return TimeHelper.formatDate(getTime(), getFormatter());
   }

   //~--- set methods ---------------------------------------------------------

   public void setFormatter(ThreadLocal<SimpleDateFormat> formatter) {
      if (formatterProperty == null) {
         this.formatter = formatter;
      } else {
         formatterProperty.set(formatter);
      }
   }

   public void setTime(long time) {
      if (timeProperty == null) {
         this.time = time;
      } else {
         timeProperty.set(time);
      }
   }
}
