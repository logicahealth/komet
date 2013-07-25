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



package org.ihtsdo.otf.tcc.datastore.id;

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author kec
 */
public class RelationshipIndexVersion {
   protected int characteristicNid;
   protected int groupId;
   protected int stamp;

   //~--- constructors --------------------------------------------------------

   public RelationshipIndexVersion(int stamp, int characteristicNid, int groupId) {
      this.stamp             = stamp;
      this.characteristicNid = characteristicNid;
      this.groupId           = groupId;
   }

   //~--- get methods ---------------------------------------------------------

   public int getAuthorNid() {
      return Ts.get().getAuthorNidForStamp(stamp);
   }

   public int getCharacteristicNid() {
      return characteristicNid;
   }

   public int getGroupId() {
      return groupId;
   }

   public int getModuleNid() {
      return Ts.get().getModuleNidForStamp(stamp);
   }

   public int getPathNid() {
      return Ts.get().getPathNidForStamp(stamp);
   }

   public int getStamp() {
      return stamp;
   }

   public Status getStatus() {
      return Ts.get().getStatusForStamp(stamp);
   }

   public long getTime() {
      return Ts.get().getTimeForStamp(stamp);
   }
}
