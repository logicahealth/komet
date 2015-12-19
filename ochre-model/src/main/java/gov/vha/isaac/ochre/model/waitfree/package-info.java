/*
 * Copyright 2015 kec.
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

/**
 * CAS on objects utilizes the object pointer (identity) to determine 
 * if the current value == the expected value.
 * 
 * The original object is a byte array, where the 1st 64 bits is a long 
 * value that provides an object version identifier. 
 * 
 * To write an updated object, perform a get, then compare the 1st 64 bits with
 * the first 64 bits of the object to be written. If they are the same, then there 
 * have been no writes between when the new object was read, and write of the 
 * new object can proceed without a merge. 
 * 
 * If they are not the same, then the objects must be merged, and then write attempted again.
 * 
 * Write sequences can be allocated in parallel based on a modulus of the object key,
 * as a key to the sequence allocator.
 * 
 * Each version has a stamp, and these stamps uniquely identify the versions, and 
 * merger can be performed by creating a set union of the versions as identified by
 * their stamps.
 * 
 * Fields: 
 *  Write sequence
 *  Native ID
 *  Container Sequence
 *  stamp
 *  length
 *  data...
 *  stamp
 *  length
 *  data...
 *  stamp // if stamp < 0; then end of record.
*/
package gov.vha.isaac.ochre.model.waitfree;
