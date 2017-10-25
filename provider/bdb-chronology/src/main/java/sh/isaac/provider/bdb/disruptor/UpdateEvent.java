/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.provider.bdb.disruptor;

//~--- non-JDK imports --------------------------------------------------------

import com.lmax.disruptor.EventFactory;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.provider.bdb.taxonomy.TaxonomyRecord;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class UpdateEvent {
   public static final EventFactory<UpdateEvent> factory = () -> new UpdateEvent();

   //~--- fields --------------------------------------------------------------

   private int            updateKey = Integer.MAX_VALUE;
   private UpdateAction   updateAction;
   private Chronology     updateObject;
   private TaxonomyRecord taxonomyUpdate;

   //~--- methods -------------------------------------------------------------

   public void clear() {
      updateKey      = Integer.MAX_VALUE;
      taxonomyUpdate = null;
      updateObject   = null;
      updateAction   = null;
   }

   //~--- get methods ---------------------------------------------------------

   public TaxonomyRecord getTaxonomyRecord() {
      if (taxonomyUpdate == null) {
         taxonomyUpdate = new TaxonomyRecord();
      }

      return taxonomyUpdate;
   }

   public UpdateAction getUpdateAction() {
      return updateAction;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUpdateAction(UpdateAction updateAction) {
      this.updateAction = updateAction;
   }

   //~--- get methods ---------------------------------------------------------

   public int getUpdateKey() {
      return updateKey;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUpdateKey(int updateKey) {
      this.updateKey = updateKey;
   }

   //~--- get methods ---------------------------------------------------------

   public Chronology getUpdateObject() {
      return updateObject;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUpdateObject(Chronology updateObject) {
      this.updateObject = updateObject;
   }

   @Override
   public String toString() {
      if (updateKey == Integer.MAX_VALUE) {
         return "UpdateEvent: empty";
      }
      return "UpdateEvent{" + "updateKey=" + updateKey + ", " + updateAction + ", " + updateObject + ", taxonomyUpdate=" + taxonomyUpdate + '}';
   }
}

