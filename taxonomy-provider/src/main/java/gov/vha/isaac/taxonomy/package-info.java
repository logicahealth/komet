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
 * Provides the framework for representing a STAMP versioned taxonomy which preserves
 * the distinctions between stated and inferred taxonomic relationships, and allows
 * storing of these records in a navigable, lock-free data structure that minimizes
 * memory requirements by making extensive use of primitive data structures, and
 * only instantiating objects from serialized data on demand. 
 * 
 */
package gov.vha.isaac.taxonomy;
