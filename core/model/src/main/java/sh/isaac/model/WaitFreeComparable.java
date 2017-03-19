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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.model;

/**
 *
 * {@code WaitFreeComparable} objects can compare the write sequence of the
 * original {@code byte[]} data from which they where deserialized, with
 * the current write sequence in a map of objects.
 * This ability to compare original
 * write sequence with current write sequence, enables compare and swap
 * updates to maps so that they may be updated using wait-free algorithms
 * (an algorithm where there is guaranteed per-thread progress). Wait-freedom is
 * the strongest non-blocking guarantee of progress).
 *
 * Also see http://minborgsjavapot.blogspot.com/2014/12/java-8-byo-super-efficient-maps.html
 * for discussion of using maps with known keyspace such as we do here...
 * @author kec
 */
public interface WaitFreeComparable {
   /**
    *
    * @return the write sequence from which this object was created.
    */
   int getWriteSequence();

   //~--- set methods ---------------------------------------------------------

   /**
    *
    * @param sequence the write sequence for which this object is to be written.
    */
   void setWriteSequence(int sequence);
}

