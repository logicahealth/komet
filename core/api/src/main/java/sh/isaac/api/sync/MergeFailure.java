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



package sh.isaac.api.sync;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MergeFailure}
 * Thrown when an operation encounters a merge failure that requires user instruction to resolve.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MergeFailure
        extends Exception {
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private final Set<String> filesWithMergeFailures_;
   private final Set<String> filesChangedDuringMergeAttempt_;

   //~--- constructors --------------------------------------------------------

   public MergeFailure(Set<String> filesWithMergeFailures, Set<String> filesChangedDuringMergeAttempt) {
      super("Merge Failure");
      this.filesWithMergeFailures_         = filesWithMergeFailures;
      this.filesChangedDuringMergeAttempt_ = filesChangedDuringMergeAttempt;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return All files that were changed (successfully or not) during the merge.
    */
   public Set<String> getFilesChangedDuringMergeAttempt() {
      return this.filesChangedDuringMergeAttempt_;
   }

   /**
    * @see java.lang.Throwable#getLocalizedMessage()
    */
   @Override
   public String getLocalizedMessage() {
      return super.getLocalizedMessage() + " on " + this.filesWithMergeFailures_ + " while updating the files " +
             this.filesChangedDuringMergeAttempt_;
   }

   /**
    * @return The files that were left in a conflicted, unusable state - much be corrected with a call to resolveMergeFailures.
    */
   public Set<String> getMergeFailures() {
      return this.filesWithMergeFailures_;
   }
}

