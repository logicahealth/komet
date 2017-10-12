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

//~--- JDK imports ------------------------------------------------------------

import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SemanticSequenceSet;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticServiceTyped;

//~--- classes ----------------------------------------------------------------

/**
 * The Class AssemblageOfTypeProvider.
 *
 * @author kec
 */
public class AssemblageOfTypeProvider
         implements SemanticServiceTyped {
   /** The type. */
   VersionType type;

   /** The sememe provider. */
   AssemblageService sememeProvider;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sememe type provider.
    *
    * @param type the type
    * @param sememeProvider the sememe provider
    */
   public AssemblageOfTypeProvider(VersionType type, AssemblageService sememeProvider) {
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
   public void writeSemanticChronology(SemanticChronology sememeChronicle) {
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
   public Stream<SemanticChronology> getParallelSemanticChronologyStream() {
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
   public SemanticChronology getSemanticChronology(int sememeSequence) {
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
   public SemanticSequenceSet getSemanticSequencesForComponent(int componentNid) {
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
   public SemanticSequenceSet getSemanticSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence) {
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
   public SemanticSequenceSet getSemanticSequencesForComponentsFromAssemblage(NidSet componentNidSet,
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
   public SemanticSequenceSet getSemanticSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
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
   public SemanticSequenceSet getSemanticSequencesFromAssemblage(int assemblageSequence) {
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
   public SemanticSequenceSet getSemanticSequencesFromAssemblageModifiedAfterPosition(int assemblageSequence,
         StampPosition position) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe stream.
    *
    * @param <C>
    * @return the sememe stream
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStream() {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememes for component.
    *
    * @param <C>
    * @param componentNid the component nid
    * @return the sememes for component
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologiesForComponent(int componentNid) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememes for component from assemblage.
    *
    * @param <C>
    * @param componentNid the component nid
    * @param assemblageSequence the assemblage sequence
    * @return the sememes for component from assemblage
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologiesForComponentFromAssemblage(int componentNid, int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememes from assemblage.
    *
    * @param <C>
    * @param assemblageSequence the assemblage sequence
    * @return the sememes from assemblage
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologiesFromAssemblage(int assemblageSequence) {
      // TODO Implement the Sememe Type provider class
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }
}

