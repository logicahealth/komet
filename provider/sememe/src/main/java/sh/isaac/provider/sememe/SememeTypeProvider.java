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



package sh.isaac.provider.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeService;
import sh.isaac.api.component.sememe.SememeServiceTyped;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampPosition;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <V>
 */
public class SememeTypeProvider<V extends SememeVersion<?>>
         implements SememeServiceTyped<V> {
   Class<V>      type;
   SememeService sememeProvider;

   //~--- constructors --------------------------------------------------------

   public SememeTypeProvider(Class<V> type, SememeService sememeProvider) {
      this.type           = type;
      this.sememeProvider = sememeProvider;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void writeSememe(SememeChronology<?> sememeChronicle) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Stream<SememeChronology<V>> getParallelSememeStream() {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SememeChronology<V> getSememe(int sememeSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet,
         int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageSequence,
         StampPosition position) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblageModifiedAfterPosition(int assemblageSequence,
         StampPosition position) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<SememeChronology<V>> getSememeStream() {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<SememeChronology<V>> getSememesForComponent(int componentNid) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<SememeChronology<V>> getSememesForComponentFromAssemblage(int componentNid, int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<SememeChronology<V>> getSememesFromAssemblage(int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }
}

