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
package sh.isaac.model.semantic.version.brittle;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.semantic.version.AbstractVersionImpl;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Nid2_Version;

/**
 *
 * @author kec
 */
public class Nid1_Nid2_VersionImpl 
        extends AbstractVersionImpl
         implements Nid1_Nid2_Version {

   int nid1 = Integer.MAX_VALUE;
   int nid2 = Integer.MAX_VALUE;

   public Nid1_Nid2_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getNid1() {
      return nid1;
   }

   @Override
   public void setNid1(int nid1) {
      this.nid1 = nid1;
   }

   @Override
   public int getNid2() {
      return nid2;
   }

   @Override
   public void setNid2(int nid2) {
      this.nid2 = nid2;
   }
   
}
