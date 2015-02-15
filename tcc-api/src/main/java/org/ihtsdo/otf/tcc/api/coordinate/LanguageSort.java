/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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

//~--- enums ---------------------------------------------------------------

public enum LanguageSort {
    LANG_BEFORE_TYPE("language before type"), TYPE_BEFORE_LANG("type before language"), LANG_REFEX("use language refex"), RF2_LANG_REFEX("use RF2 language refex");
    private String desc;

    //~--- constructors -----------------------------------------------------
    private LanguageSort(String desc) {
        this.desc = desc;
    }

    //~--- methods ----------------------------------------------------------
    @Override
    public String toString() {
        return desc;
    }
    
}
