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



package sh.isaac.provider.assemblage;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The Class AssemblageSememeKey.
 *
 * @author kec
 */
public class AssemblageSememeKey
         implements Comparable<AssemblageSememeKey>, Externalizable {
   /** The assemblage sequence. */
   int assemblageSequence;

   /** The sememe sequence. */
   int sememeSequence;

   
   //~--- constructors --------------------------------------------------------

   public AssemblageSememeKey() {
   }

   /**
    * Instantiates a new assemblage sememe key.
    *
    * @param assemblageKey the assemblage key
    * @param sememeSequence the sememe sequence
    */
   public AssemblageSememeKey(int assemblageKey, int sememeSequence) {
      this.assemblageSequence = assemblageKey;
      this.sememeSequence     = sememeSequence;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(AssemblageSememeKey o) {
      if (this.assemblageSequence != o.assemblageSequence) {
         if (this.assemblageSequence < o.assemblageSequence) {
            return -1;
         }

         return 1;
      }

      if (this.sememeSequence == o.sememeSequence) {
         return 0;
      }

      if (this.sememeSequence < o.sememeSequence) {
         return -1;
      }

      return 1;
   }

   /**
    * Equals.
    *
    * @param o the o
    * @return true, if successful
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      final AssemblageSememeKey otherKey = (AssemblageSememeKey) o;

      if (this.assemblageSequence != otherKey.assemblageSequence) {
         return false;
      }

      return this.sememeSequence == otherKey.sememeSequence;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = this.assemblageSequence;

      result = 31 * result + this.sememeSequence;
      return result;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "AssemblageSememeKey{" + "assemblageSequence=" + this.assemblageSequence + ", sememeSequence=" +
             this.sememeSequence + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the assemblage sequence.
    *
    * @return the assemblage sequence
    */
   public int getAssemblageSequence() {
      return this.assemblageSequence;
   }

   /**
    * Gets the sememe sequence.
    *
    * @return the sememe sequence
    */
   public int getSememeSequence() {
      return this.sememeSequence;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(assemblageSequence);
      out.writeInt(sememeSequence);
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      assemblageSequence = in.readInt();
      sememeSequence = in.readInt();
   }
}

