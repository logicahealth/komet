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
 * The Class SememeTypeProvider.
 *
 * @author kec
 * @param <V> the value type
 */
public class SememeTypeProvider<V extends SememeVersion<?>>
         implements SememeServiceTyped<V> {
   
   /** The type. */
   Class<V>      type;
   
   /** The sememe provider. */
   SememeService sememeProvider;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sememe type provider.
    *
    * @param type the type
    * @param sememeProvider the sememe provider
    */
   public SememeTypeProvider(Class<V> type, SememeService sememeProvider) {
      this.type           = type;
      this.sememeProvider = sememeProvider;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Write sememe.
    *
    * @param sememeChronicle the sememe chronicle
    */
   @Override
   public void writeSememe(SememeChronology<?> sememeChronicle) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the parallel sememe stream.
    *
    * @return the parallel sememe stream
    */
   @Override
   public Stream<SememeChronology<V>> getParallelSememeStream() {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe.
    *
    * @param sememeSequence the sememe sequence
    * @return the sememe
    */
   @Override
   public SememeChronology<V> getSememe(int sememeSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe sequences for component.
    *
    * @param componentNid the component nid
    * @return the sememe sequences for component
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageSequence the assemblage sequence
    * @return the sememe sequences for component from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe sequences for components from assemblage.
    *
    * @param componentNidSet the component nid set
    * @param assemblageSequence the assemblage sequence
    * @return the sememe sequences for components from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet,
         int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe sequences for components from assemblage modified after position.
    *
    * @param componentNidSet the component nid set
    * @param assemblageSequence the assemblage sequence
    * @param position the position
    * @return the sememe sequences for components from assemblage modified after position
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageSequence,
         StampPosition position) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe sequences from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe sequences from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe sequences from assemblage modified after position.
    *
    * @param assemblageSequence the assemblage sequence
    * @param position the position
    * @return the sememe sequences from assemblage modified after position
    */
   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblageModifiedAfterPosition(int assemblageSequence,
         StampPosition position) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe stream.
    *
    * @return the sememe stream
    */
   @Override
   public Stream<SememeChronology<V>> getSememeStream() {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememes for component.
    *
    * @param componentNid the component nid
    * @return the sememes for component
    */
   @Override
   public Stream<SememeChronology<V>> getSememesForComponent(int componentNid) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememes for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageSequence the assemblage sequence
    * @return the sememes for component from assemblage
    */
   @Override
   public Stream<SememeChronology<V>> getSememesForComponentFromAssemblage(int componentNid, int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememes from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememes from assemblage
    */
   @Override
   public Stream<SememeChronology<V>> getSememesFromAssemblage(int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }
}

