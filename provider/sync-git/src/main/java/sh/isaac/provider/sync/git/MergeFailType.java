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



package sh.isaac.provider.sync.git;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.sync.MergeFailOption;

//~--- constant enums ---------------------------------------------------------

/**
 * {@link MergeFailType}
 *
 * During update and commit operations, if there is uncommitted work, we have to stash, if we are using git.
 * During the apply of stash, we may have a merge failure, if the incoming changes also changed the same files
 * that are changed in the stash.  Unfortunately, The notion of "OURS" and "THEIRS" which correspond to
 * {@link MergeFailOption#KEEP_LOCAL} and {@link MergeFailOption#KEEP_REMOTE} reverse, depending on whether we are
 * fixing a conflict that resulted from the merge failure, or a conflict that resulted from the stash apply failure.
 * So, the caller will have to inform us which type of merge failure they are fixing.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum MergeFailType { REMOTE_TO_LOCAL,
                            STASH_TO_LOCAL; }

