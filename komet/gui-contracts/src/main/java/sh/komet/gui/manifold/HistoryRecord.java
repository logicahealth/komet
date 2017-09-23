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
package sh.komet.gui.manifold;

import java.util.Objects;

/**
 *
 * @author kec
 */
public class HistoryRecord {
   int componentId;
   String componentString;

   public HistoryRecord(int componentId, String componentString) {
      this.componentId = componentId;
      this.componentString = componentString;
   }

   public int getComponentId() {
      return componentId;
   }

   public String getComponentString() {
      return componentString;
   }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 83 * hash + this.componentId;
      hash = 83 * hash + Objects.hashCode(this.componentString);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final HistoryRecord other = (HistoryRecord) obj;
      if (this.componentId != other.componentId) {
         return false;
      }
      return Objects.equals(this.componentString, other.componentString);
   }

   @Override
   public String toString() {
      return "HistoryRecord{" + "componentId=" + componentId + ", componentString=" + componentString + '}';
   }
   
}
