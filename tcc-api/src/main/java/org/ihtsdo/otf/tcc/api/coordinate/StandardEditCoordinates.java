/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.api.coordinate;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;

/**
 *
 * @author aimeefurber
 */
public class StandardEditCoordinates {
   public static EditCoordinate getDefaultUserSnomedCore() throws IOException {
       
       EditCoordinate editCoordinate = new EditCoordinate(TermAux.USER.getLenient().getNid(),
               Snomed.CORE_MODULE.getLenient().getNid(),
               TermAux.SNOMED_CORE.getLenient().getNid());

      return editCoordinate;
   }
   
   public static EditCoordinate getDefaultUserWorkbenchAux() throws IOException {
       
       EditCoordinate editCoordinate = new EditCoordinate(TermAux.USER.getLenient().getNid(),
               TermAux.TERM_AUX_MODULE.getLenient().getNid(),
               TermAux.WB_AUX_PATH.getLenient().getNid());

      return editCoordinate;
   }
}
