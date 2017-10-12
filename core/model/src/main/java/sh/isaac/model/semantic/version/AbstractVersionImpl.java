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
package sh.isaac.model.semantic.version;

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.VersionImpl;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.MutableSemanticVersion;

/**
 *
 * @author kec
 */
public abstract class AbstractVersionImpl 
        extends VersionImpl
         implements MutableSemanticVersion {
   /**
    * Instantiates a new sememe version impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    */
   public AbstractVersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }
   

   private AbstractVersionImpl(SemanticVersionImpl other, int stampSequence) {
      super(other.getChronology(), stampSequence);
   }

   //~--- methods -------------------------------------------------------------
   public abstract VersionType getSememeType();
   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the assemblage sequence.
    *
    * @return the assemblage sequence
    */
   @Override
   public final int getAssemblageSequence() {
      return getChronology().getAssemblageSequence();
   }

   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   public final SemanticChronology getChronology() {
      return (SemanticChronology) this.chronicle;
   }

   /**
    * Gets the referenced component nid.
    *
    * @return the referenced component nid
    */
   @Override
   public final int getReferencedComponentNid() {
      return getChronology().getReferencedComponentNid();
   }

   /**
    * Gets the sememe sequence.
    *
    * @return the sememe sequence
    */
   @Override
   public final int getSemanticSequence() {
      return getChronology().getSemanticSequence();
   }

   @Override
   protected final int editDistance2(VersionImpl other, int editDistance) {
      return editDistance3((AbstractVersionImpl) other, editDistance);
   }
   
   protected abstract int editDistance3(AbstractVersionImpl other, int editDistance);
   
   @Override
   protected final boolean deepEquals2(VersionImpl other) {
      if (!(other instanceof AbstractVersionImpl)) {
         return false;
      }
      AbstractVersionImpl otherVersion = (AbstractVersionImpl) other;
      return deepEquals3(otherVersion);
   }
   protected abstract boolean deepEquals3(AbstractVersionImpl other);
   
}


