/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */



package org.ihtsdo.otf.tcc.api.id;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI;

/**
 *
 * @author kec
 */
public interface IdBI extends VersionPointBI {
   Set<Integer> getAllNidsForId() throws IOException;

   int getAuthorNid();

   int getAuthorityNid();

   Object getDenotation();

   int getStamp();

   Status getStatus();
   
   int getModuleNid();
}
