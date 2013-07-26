/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.chronicle.cc;

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate.LANGUAGE_SORT;

/**
 *
 * @author kec
 */
public class LanguageSortPrefs {

    public enum LANGUAGE_SORT_PREF {
        TYPE_B4_LANG("type before language"),
        LANG_REFEX("use language refex"), 
        RF2_LANG_REFEX("use RF2 language refex");

        private String desc;

        private LANGUAGE_SORT_PREF(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        public LANGUAGE_SORT getLangSort() {
            switch (this) {
                case LANG_REFEX:
                    return LANGUAGE_SORT.LANG_REFEX;
                case TYPE_B4_LANG:
                    return LANGUAGE_SORT.TYPE_BEFORE_LANG;
                case RF2_LANG_REFEX:
                    return LANGUAGE_SORT.RF2_LANG_REFEX;
            }
            throw new UnsupportedOperationException("Can't handle: " + this);
        }
        public static LANGUAGE_SORT_PREF  getPref(LANGUAGE_SORT sort) {
            switch (sort) {
                 case LANG_REFEX:
                    return LANG_REFEX;
                case TYPE_BEFORE_LANG:
                    return TYPE_B4_LANG;
                case RF2_LANG_REFEX:
                    return RF2_LANG_REFEX;
            }
            throw new UnsupportedOperationException("Can't handle: " + sort);
        }
    }
    
}
